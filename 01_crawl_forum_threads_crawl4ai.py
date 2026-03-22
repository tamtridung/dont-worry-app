import warnings
warnings.filterwarnings(
    "ignore",
    message=r"urllib3 \(.*\) or chardet \(.*\)/charset_normalizer \(.*\) doesn't match a supported version!",
)

import argparse
import asyncio
import re
from dataclasses import dataclass
from typing import Iterable
from urllib.parse import urljoin

import yaml
from bs4 import BeautifulSoup
from crawl4ai import AsyncWebCrawler, BrowserConfig, CrawlerRunConfig


FORUM_PAGE_URL_TEMPLATE = (
    "https://www.diendanhiv.vn/forums/21-Hoi-Va-Dap-Quan-he-tinh-duc-bao-cao-su-chat-boi-tron/page{page}"
)
SITE_BASE_URL = "https://www.diendanhiv.vn/"


THREAD_ID_RE = re.compile(r"threads/(?P<id>\d+)-")
DATE_TIME_RE = re.compile(r"(?P<date>\d{2}-\d{2}-\d{4})\s*(?P<time>\d{2}:\d{2})")
STARTED_BY_RE = re.compile(r"Bắt đầu bởi\s*(?P<name>[^,]+)", re.IGNORECASE)


@dataclass(frozen=True)
class ThreadInfo:
    thread_title: str
    thread_link: str
    created_date: str
    created_by: str
    thread_id: str | None = None

    def to_yaml_dict(self) -> dict:
        return {
            "thread-title": self.thread_title,
            "thread-link": self.thread_link,
            "created-date": self.created_date,
            "created-by": self.created_by,
        }


def _extract_thread_id(href: str) -> str | None:
    match = THREAD_ID_RE.search(href)
    return match.group("id") if match else None


def _normalize_whitespace(text: str) -> str:
    return re.sub(r"\s+", " ", text).strip()


def _parse_created_date(*candidate_texts: str) -> str:
    """Return a best-effort created date string.

    Preference order:
    1) Exact dd-mm-yyyy hh:mm (from any candidate text)
    2) Fallback to whatever appears after the first comma (e.g., 'Hôm nay 15:10')
    """

    for text in candidate_texts:
        if not text:
            continue
        match = DATE_TIME_RE.search(text)
        if match:
            return f"{match.group('date')} {match.group('time')}"

    for text in candidate_texts:
        if not text:
            continue
        if "," in text:
            tail = text.split(",", 1)[1]
            tail = _normalize_whitespace(tail)
            if tail:
                return tail

    return ""


def _parse_created_by(label_text: str, fallback_title: str) -> str:
    if label_text:
        match = STARTED_BY_RE.search(label_text)
        if match:
            return _normalize_whitespace(match.group("name"))

    if fallback_title:
        match = STARTED_BY_RE.search(fallback_title)
        if match:
            return _normalize_whitespace(match.group("name"))

    return ""


def parse_threads_from_forum_html(html: str, base_url: str = SITE_BASE_URL) -> list[ThreadInfo]:
    soup = BeautifulSoup(html, "lxml")

    thread_items = soup.select("li.threadbit")
    threads: list[ThreadInfo] = []

    for li in thread_items:
        title_el = li.select_one("h3.threadtitle a.title")
        if not title_el:
            continue

        title_text = _normalize_whitespace(title_el.get_text(" ", strip=True))
        href = (title_el.get("href") or "").strip()
        if not href:
            continue

        thread_link = urljoin(base_url, href)
        thread_id = _extract_thread_id(href)

        label_el = li.select_one(".threadmeta .author .label")
        label_text = _normalize_whitespace(label_el.get_text(" ", strip=True)) if label_el else ""

        created_by_el = li.select_one(".threadmeta .author .label a.username")
        created_by_title = (created_by_el.get("title") or "").strip() if created_by_el else ""
        created_by = (
            _normalize_whitespace(created_by_el.get_text(" ", strip=True))
            if created_by_el and created_by_el.get_text(strip=True)
            else _parse_created_by(label_text, created_by_title)
        )

        created_date = _parse_created_date(label_text, created_by_title)

        threads.append(
            ThreadInfo(
                thread_title=title_text,
                thread_link=thread_link,
                created_date=created_date,
                created_by=created_by,
                thread_id=thread_id,
            )
        )

    return threads


def dedupe_threads(threads: Iterable[ThreadInfo]) -> list[ThreadInfo]:
    seen: set[str] = set()
    unique: list[ThreadInfo] = []

    for t in threads:
        key = t.thread_id or t.thread_link
        if key in seen:
            continue
        seen.add(key)
        unique.append(t)

    return unique


async def crawl_forum_pages(start_page: int, end_page: int, delay_seconds: float) -> list[ThreadInfo]:
    urls = [FORUM_PAGE_URL_TEMPLATE.format(page=p) for p in range(start_page, end_page + 1)]

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

    run_config = CrawlerRunConfig(
        page_timeout=60000,
        remove_overlay_elements=True,
    )

    all_threads: list[ThreadInfo] = []

    async with AsyncWebCrawler(config=browser_config) as crawler:
        for idx, url in enumerate(urls, start=1):
            result = await crawler.arun(url=url, config=run_config)
            if not result.success:
                raise RuntimeError(f"Crawl failed for {url}: {result.error_message}")

            page_threads = parse_threads_from_forum_html(result.html, base_url=SITE_BASE_URL)
            all_threads.extend(page_threads)

            if delay_seconds > 0 and idx < len(urls):
                await asyncio.sleep(delay_seconds)

    return dedupe_threads(all_threads)


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Crawl diendanhiv.vn forum pages with Crawl4AI and export thread info to YAML."
    )
    parser.add_argument("--start-page", type=int, default=1)
    parser.add_argument("--end-page", type=int, default=5)
    parser.add_argument("--delay", type=float, default=1.0, help="Delay between pages (seconds)")
    parser.add_argument(
        "--out",
        default="thread-info_page1-5.yaml",
        help="Output YAML file path",
    )

    args = parser.parse_args()

    if args.start_page < 1:
        raise SystemExit("--start-page must be >= 1")
    if args.end_page < args.start_page:
        raise SystemExit("--end-page must be >= --start-page")

    threads = asyncio.run(crawl_forum_pages(args.start_page, args.end_page, args.delay))

    payload = [t.to_yaml_dict() for t in threads]

    with open(args.out, "w", encoding="utf-8") as f:
        yaml.safe_dump(payload, f, allow_unicode=True, sort_keys=False)

    print(f"Wrote {len(payload)} threads to {args.out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
