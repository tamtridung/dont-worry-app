import argparse
import asyncio
import math
import re
import warnings
from dataclasses import dataclass
from typing import Any
from urllib.parse import urljoin
from copy import deepcopy
from typing import List, Dict, Any

import yaml
from bs4 import BeautifulSoup
warnings.filterwarnings(
    "ignore",
    message=r"urllib3 \(.*\) or chardet \(.*\)/charset_normalizer \(.*\) doesn't match a supported version!",
)

from crawl4ai import AsyncWebCrawler, BrowserConfig, CrawlerRunConfig


SITE_BASE_URL = "https://www.diendanhiv.vn/"

THREAD_ID_RE = re.compile(r"/threads/(?P<id>\d+)(?:-|\b)")
POST_STATS_RE = re.compile(
    r"Kết\s+quả\s+(?P<start>\d+)\s+đến\s+(?P<end>\d+)\s+của\s+(?P<total>\d+)",
    re.IGNORECASE,
)


@dataclass(frozen=True)
class Post:
    author: str
    content: str



def _clean_responses(responses: List[Dict[str, Any]], thread_content: str) -> List[Dict[str, str]]:
    def norm_ws(s: str) -> str:
        return re.sub(r"\s+", " ", (s or "")).strip()

    cleaned: List[Dict[str, str]] = []
    prior_texts: List[str] = []  # các nội dung đã clean trước đó để remove khỏi quote

    for item in responses:
        content = item.get("response-content", "") or ""

        # 1) Bỏ thread_content nếu bị dính vào response-content
        if thread_content:
            content = content.replace(thread_content, "")

        # 2) Bỏ các đoạn quote lại từ các phản hồi trước
        #    (thường là copy nguyên văn nội dung bài trước)
        for prev in prior_texts:
            if prev and prev in content:
                content = content.replace(prev, "")

        # 3) Bỏ prefix kiểu "Gửi bởi <username>" nếu còn sót ở đầu
        content = re.sub(r"^\s*Gửi bởi\s+\S+\s*", "", content)

        # 4) Chuẩn hoá khoảng trắng
        content = norm_ws(content)

        cleaned.append({
            "response-content": content,
            "responser": item.get("responser", "") or "",
        })

        if content:
            prior_texts.append(content)

    return cleaned

def _normalize_whitespace(text: str) -> str:
    return re.sub(r"\s+", " ", (text or "")).strip()


def extract_thread_id(thread_link: str) -> str:
    match = THREAD_ID_RE.search(thread_link)
    if not match:
        raise ValueError(f"Cannot extract thread id from link: {thread_link}")
    return match.group("id")


def extract_thread_title(soup: BeautifulSoup) -> str:
    # Preferred: page title block
    el = soup.select_one("#pagetitle .threadtitle a")
    if el and el.get_text(strip=True):
        return _normalize_whitespace(el.get_text(" ", strip=True))

    # Fallback: first post title
    el = soup.select_one("ol#posts h2.title")
    if el and el.get_text(strip=True):
        return _normalize_whitespace(el.get_text(" ", strip=True))

    # Fallback: <title>
    if soup.title and soup.title.get_text(strip=True):
        return _normalize_whitespace(soup.title.get_text(" ", strip=True))

    return ""


def extract_post_stats(soup: BeautifulSoup) -> tuple[int, int, int] | None:
    stats_el = soup.select_one("#postpagestats_above") or soup.select_one("#postpagestats")
    if not stats_el:
        return None

    text = _normalize_whitespace(stats_el.get_text(" ", strip=True))
    match = POST_STATS_RE.search(text)
    if not match:
        return None

    return int(match.group("start")), int(match.group("end")), int(match.group("total"))


def extract_posts_from_thread_html(html: str) -> list[Post]:
    soup = BeautifulSoup(html, "lxml")

    posts: list[Post] = []
    post_items = soup.select("ol#posts > li.postcontainer[id^=post_]")

    for li in post_items:
        author_el = li.select_one(".postdetails a.username") or li.select_one("a.username")
        author = _normalize_whitespace(author_el.get_text(" ", strip=True)) if author_el else ""

        message_div = li.select_one("div[id^=post_message_]")
        if not message_div:
            continue

        content_el = message_div.select_one("blockquote.postcontent")
        if not content_el:
            continue

        content = _normalize_whitespace(content_el.get_text(" ", strip=True))
        posts.append(Post(author=author, content=content))

    return posts


async def arun_with_retries(
    crawler: AsyncWebCrawler,
    url: str,
    config: CrawlerRunConfig,
    retries: int,
    retry_delay_seconds: float,
):
    last_error: str | None = None
    for attempt in range(1, retries + 2):
        result = await crawler.arun(url=url, config=config)
        if result.success:
            return result

        last_error = result.error_message
        if attempt <= retries:
            await asyncio.sleep(retry_delay_seconds)

    raise RuntimeError(f"Crawl failed for {url}: {last_error}")


async def crawl_single_thread(
    crawler: AsyncWebCrawler,
    thread_link: str,
    delay_seconds: float,
    max_pages: int | None,
) -> dict[str, Any]:
    thread_id = extract_thread_id(thread_link)

    run_config = CrawlerRunConfig(
        page_timeout=120000,
        remove_overlay_elements=False,
    )

    # Page 1: try the provided SEO link first; if blocked/empty, fall back to showthread.php
    primary_url = thread_link
    fallback_url = f"{SITE_BASE_URL}showthread.php?t={thread_id}&page=1"

    result = await arun_with_retries(
        crawler=crawler,
        url=primary_url,
        config=run_config,
        retries=2,
        retry_delay_seconds=2.0,
    )

    soup = BeautifulSoup(result.html, "lxml")
    thread_title = extract_thread_title(soup)

    all_posts: list[Post] = extract_posts_from_thread_html(result.html)

    if not all_posts:
        result = await arun_with_retries(
            crawler=crawler,
            url=fallback_url,
            config=run_config,
            retries=2,
            retry_delay_seconds=2.0,
        )
        soup = BeautifulSoup(result.html, "lxml")
        thread_title = extract_thread_title(soup) or thread_title
        all_posts = extract_posts_from_thread_html(result.html)

    # Determine how many pages to crawl (if stats are available)
    stats = extract_post_stats(soup)
    total_pages = 1
    if stats:
        start, end, total = stats
        page_size = max(1, end - start + 1)
        if total > end:
            total_pages = int(math.ceil(total / page_size))

    if max_pages is not None:
        total_pages = min(total_pages, max_pages)

    # Pages 2..N: use stable vBulletin paging URL
    for page in range(2, total_pages + 1):
        page_url = f"{SITE_BASE_URL}showthread.php?t={thread_id}&page={page}"
        page_result = await arun_with_retries(
            crawler=crawler,
            url=page_url,
            config=run_config,
            retries=2,
            retry_delay_seconds=2.0,
        )

        page_posts = extract_posts_from_thread_html(page_result.html)
        all_posts.extend(page_posts)

        if delay_seconds > 0:
            await asyncio.sleep(delay_seconds)

    thread_content = all_posts[0].content if all_posts else ""

    responses = [
        {
            "response-content": p.content,
            "responser": p.author,
        }
        for p in (all_posts[1:] if len(all_posts) > 1 else [])
    ]

    # clean repsonses:
    responses = _clean_responses(
        responses=responses, 
        thread_content=thread_content)


    return {
        "thread-title": thread_title,
        "thread-id": thread_id,
        "thread-content": thread_content,
        "responses": responses,
    }


async def crawl_threads(
    thread_links: list[str],
    delay_seconds: float,
    max_threads: int | None,
    max_pages_per_thread: int | None,
    fail_fast: bool,
) -> list[dict[str, Any]]:
    browser_config = BrowserConfig(
        headless=True,
        viewport_width=1920,
        viewport_height=1080,
        user_agent=(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/123.0.0.0 Safari/537.36"
        ),
    )

    links = thread_links
    if max_threads is not None:
        links = links[: max(0, max_threads)]

    results: list[dict[str, Any]] = []

    async with AsyncWebCrawler(config=browser_config) as crawler:
        for idx, link in enumerate(links, start=1): # <==== limit to 5 threads for testing
            try:
                data = await crawl_single_thread(
                    crawler=crawler,
                    thread_link=link,
                    delay_seconds=delay_seconds,
                    max_pages=max_pages_per_thread,
                )
                results.append(data)
                print(f"[{idx}/{len(links)}] OK thread-id={data.get('thread-id')}")
            except Exception as exc:
                print(f"[{idx}/{len(links)}] ERROR {link}: {exc}")
                if fail_fast:
                    raise

            if delay_seconds > 0:
                await asyncio.sleep(delay_seconds)

    return results


def load_thread_links_from_yaml(path: str) -> list[str]:
    with open(path, "r", encoding="utf-8") as f:
        items = yaml.safe_load(f) or []

    if not isinstance(items, list):
        raise ValueError("Input YAML must be a list")

    links: list[str] = []
    for item in items:
        if not isinstance(item, dict):
            continue
        link = (item.get("thread-link") or "").strip()
        if link:
            links.append(link)

    return links


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Crawl vBulletin thread pages with Crawl4AI and export thread content + responses to YAML."
    )
    parser.add_argument(
        "--in",
        dest="in_path",
        default="data/thread-info-2025-03-22.yaml",
        help="Input YAML file with thread-link entries",
    )
    parser.add_argument(
        "--out",
        default="data/thread-details-2025-03-22.yaml",
        help="Output YAML path",
    )
    parser.add_argument("--delay", type=float, default=1.0, help="Delay between requests (seconds)")
    parser.add_argument(
        "--max-threads",
        type=int,
        default=None,
        help="Limit number of threads for testing",
    )
    parser.add_argument(
        "--max-pages-per-thread",
        type=int,
        default=None,
        help="Limit number of pages per thread (page>=1). Useful for very long threads.",
    )
    parser.add_argument(
        "--fail-fast",
        action="store_true",
        help="Stop on first crawl/parse error",
    )

    args = parser.parse_args()

    thread_links = load_thread_links_from_yaml(args.in_path)
    if not thread_links:
        raise SystemExit(f"No thread links found in {args.in_path}")

    data = asyncio.run(
        crawl_threads(
            thread_links=thread_links,
            delay_seconds=args.delay,
            max_threads=args.max_threads,
            max_pages_per_thread=args.max_pages_per_thread,
            fail_fast=args.fail_fast,
        )
    )

    with open(args.out, "w", encoding="utf-8") as f:
        yaml.safe_dump(data, f, allow_unicode=True, sort_keys=False)

    print(f"Wrote {len(data)} threads to {args.out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
