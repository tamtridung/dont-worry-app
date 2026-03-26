# Hướng dẫn chi tiết cập nhật dữ liệu diễn đàn cho app Android

Tài liệu này mô tả quy trình đầy đủ để lấy dữ liệu mới từ diễn đàn, xuất thành YAML, kiểm tra chất lượng dữ liệu, và cập nhật vào app Android.

## 1. Tổng quan luồng dữ liệu

App Android đang đọc dữ liệu offline từ thư mục assets:

- `android/app/src/main/assets/data/thread-info.yaml`
- `android/app/src/main/assets/data/thread-details.yaml`
- `android/app/src/main/assets/synonyms/synonyms.yaml`

Nguồn dữ liệu crawl và dữ liệu tạm trong quá trình xử lý thường nằm ở:

- `data/thread-info-YYYY-MM-DD.yaml`
- `data/thread-details-YYYY-MM-DD.yaml`

Quy trình chuẩn:

1. Crawl danh sách thread (metadata) -> `thread-info-*.yaml`
2. Crawl nội dung chi tiết từng thread -> `thread-details-*.yaml`
3. Kiểm tra cấu trúc và chất lượng dữ liệu
4. Copy/đổi tên file mới sang assets Android
5. Build và smoke test app

---

## 2. Chuẩn bị môi trường

### 2.1 Yêu cầu

- Python 3.10+
- pip
- Android Studio (để test app sau khi cập nhật dữ liệu)

### 2.2 Cài dependencies crawl

Chạy tại root project:

```bash
pip install -r requirements.txt
```

`requirements.txt` hiện có:

- `crawl4ai`
- `beautifulsoup4`
- `PyYAML`
- `lxml`

---

## 3. Bước 1: Crawl danh sách thread

Script sử dụng: `01_crawl_forum_threads_crawl4ai.py`

### 3.1 Chạy crawl

Ví dụ crawl từ page 1 đến page 5:

```bash
python 01_crawl_forum_threads_crawl4ai.py \
  --start-page 1 \
  --end-page 5 \
  --delay 1.0 \
  --out data/thread-info-2026-03-26.yaml
```

### 3.2 Ý nghĩa tham số

- `--start-page`: trang bắt đầu
- `--end-page`: trang kết thúc
- `--delay`: thời gian nghỉ giữa các request (giây)
- `--out`: file output YAML

### 3.3 Kết quả mong đợi

File YAML list object, mỗi object có dạng:

```yaml
- thread-title: "..."
  thread-link: "https://www.diendanhiv.vn/threads/..."
  created-date: "dd-mm-yyyy hh:mm"
  created-by: "..."
```

---

## 4. Bước 2: Crawl nội dung chi tiết thread

Script sử dụng: `02_crawl_thread_details_crawl4ai.py`

### 4.1 Chạy crawl đầy đủ

```bash
python 02_crawl_thread_details_crawl4ai.py \
  --in data/thread-info-2026-03-26.yaml \
  --out data/thread-details-2026-03-26.yaml \
  --delay 1.0
```

### 4.2 Chạy nhanh để test (khuyến nghị)

```bash
python 02_crawl_thread_details_crawl4ai.py \
  --in data/thread-info-2026-03-26.yaml \
  --out data/thread-details-test.yaml \
  --max-threads 20 \
  --max-pages-per-thread 2 \
  --delay 1.0
```

### 4.3 Ý nghĩa tham số

- `--in`: file thread-info đầu vào
- `--out`: file thread-details đầu ra
- `--delay`: thời gian nghỉ giữa các request
- `--max-threads`: giới hạn số thread để test
- `--max-pages-per-thread`: giới hạn số trang mỗi thread
- `--fail-fast`: dừng ngay khi có lỗi

### 4.4 Kết quả mong đợi

Mỗi thread có dạng:

```yaml
- thread-title: "..."
  thread-id: "86298"
  thread-content: "Nội dung câu hỏi"
  responses:
    - response-content: "..."
      responser: "..."
```

---

## 5. Bước 3: Kiểm tra chất lượng dữ liệu trước khi đưa vào app

Checklist tối thiểu:

1. `thread-info` và `thread-details` đều parse được YAML (không lỗi format).
2. `thread-title` không rỗng.
3. `thread-link` hợp lệ cho phần lớn thread.
4. `responses` tồn tại (có thể rỗng ở một số thread).
5. Với response item, dùng key `response-content` và `responser`.

Gợi ý kiểm tra nhanh bằng tay:

- Mở 2 file YAML, kiểm tra 20-30 bản ghi đầu và cuối.
- Tìm các bản ghi có `responses: []` quá nhiều bất thường.

---

## 6. Bước 4: Cập nhật dữ liệu vào app Android

Sau khi chốt dữ liệu mới, copy vào assets mà app đang dùng:

```bash
cp data/thread-info-2026-03-26.yaml android/app/src/main/assets/data/thread-info.yaml
cp data/thread-details-2026-03-26.yaml android/app/src/main/assets/data/thread-details.yaml
```

Lưu ý quan trọng:

- App đọc cứng tên file `thread-info.yaml` và `thread-details.yaml` trong assets.
- Không đổi tên 2 file này ở assets nếu chưa sửa code đọc dữ liệu.

---

## 7. Cập nhật từ viết tắt / đồng nghĩa (synonyms)

App có hỗ trợ mở rộng từ khóa viết tắt qua file:

- `android/app/src/main/assets/synonyms/synonyms.yaml`

Format:

```yaml
synonyms:
  - canonical: "gai mai dam"
    terms:
      - "gmd"
      - "gai md"
      - "gai mai dam"
```

Nguyên tắc:

- `canonical`: cụm chuẩn đại diện nhóm.
- `terms`: danh sách biến thể, viết tắt, cách gõ khác nhau.
- Nên thêm cả dạng có dấu và không dấu.

Nếu file synonyms lỗi hoặc thiếu:

- App vẫn chạy.
- Chỉ tắt mở rộng đồng nghĩa (search theo từ gốc).

---

## 8. Bước 5: Build và test sau khi cập nhật data

Trong Android Studio:

1. `Build > Clean Project`
2. `Build > Rebuild Project`
3. Run app

Smoke test:

1. Nhập từ khóa phổ biến: `hiv`, `massage`, `gmd`.
2. Kiểm tra có kết quả và phân trang hoạt động.
3. Mở DetailScreen, kiểm tra nội dung câu hỏi dài có cuộn được.
4. Bấm link thread, kiểm tra mở trình duyệt được.

---

## 9. Quy trình khuyến nghị theo đợt cập nhật

1. Crawl thử nhỏ (`--max-threads`, `--max-pages-per-thread`) để kiểm tra pipeline.
2. Crawl full.
3. Kiểm tra chất lượng YAML.
4. Cập nhật assets.
5. Build + smoke test.
6. Commit với message rõ ràng, ví dụ:

```bash
git add data/ android/app/src/main/assets/
git commit -m "data: refresh forum threads 2026-03-26"
```

---

## 10. Lỗi thường gặp và cách xử lý

### 10.1 Crawl lỗi ngắt quãng hoặc timeout

- Tăng `--delay` (ví dụ 1.5-2.0 giây).
- Chạy theo batch nhỏ rồi gộp.

### 10.2 YAML parse lỗi

- Kiểm tra ký tự đặc biệt, quote, xuống dòng.
- Dùng editor có YAML lint để bắt lỗi indent.

### 10.3 App chạy nhưng search ra ít hoặc không ra kết quả

- Kiểm tra `thread-details.yaml` có `response-content` đúng key không.
- Kiểm tra dữ liệu đã copy đúng vào `android/app/src/main/assets/data/` chưa.
- Clean/Rebuild lại app để đảm bảo assets mới đã được đóng gói.

---

## 11. Ghi chú vận hành

- Giữ lại bản snapshot theo ngày trong thư mục `data/` để có thể rollback.
- Chỉ copy bản đã kiểm tra sang assets Android.
- Nếu thay đổi schema YAML, cần cập nhật parser trong app trước khi phát hành.
