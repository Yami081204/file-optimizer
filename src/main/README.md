# File Optimizer 🚀

**File Optimizer** là một ứng dụng desktop mạnh mẽ được phát triển bằng Java và JavaFX, giúp người dùng quét, phân tích và tối ưu hóa không gian lưu trữ máy tính một cách thông minh và hiệu quả.

Dự án được xây dựng với kiến trúc hiện đại, tập trung vào trải nghiệm người dùng và khả năng mở rộng thông qua hệ thống Plugin và AI.

## ✨ Tính năng nổi bật

- 📊 **System Dashboard:** Cung cấp cái nhìn tổng quan về trạng thái ổ đĩa, dung lượng đã sử dụng và các tệp rác hiện có thông qua biểu đồ trực quan.
- 🔍 **Smart Scan:** Quét sâu các thư mục để tìm kiếm tệp tạm (`.tmp`), tệp nhật ký (`.log`), và tệp có kích thước bằng 0.
- 👯 **Duplicate Finder:** Thuật toán thông minh giúp phát hiện và loại bỏ các tệp tin trùng lặp để giải phóng bộ nhớ.
- 🤖 **AI Chat Assistant:** Tích hợp trợ lý ảo hỗ trợ người dùng đưa ra các quyết định tối ưu hóa và giải đáp thắc mắc về hệ thống.
- ⚙️ **Auto Clean:** Thiết lập lịch trình tự động dọn dẹp hệ thống mà không cần can thiệp thủ công.
- 🧩 **Plugin System:** Kiến trúc mô-đun linh hoạt, dễ dàng mở rộng thêm các tính năng quét mới.

## 🛠️ Công nghệ sử dụng

- **Ngôn ngữ:** Java 17+
- **Giao diện:** JavaFX (FXML, CSS)
- **Kiến trúc:** MVVM (Model-View-ViewModel)
- **Xử lý đa luồng:** Java Concurrency (Task, Service)
- **Cơ sở dữ liệu:** SQLite

## 🚀 Hướng dẫn khởi chạy

### Yêu cầu
- JDK 17 trở lên.
- Maven.

### Các bước cài đặt
1. Clone dự án:
   ```bash
   git clone https://github.com/Yami081204/file-optimizer.git
   ```
2. Cài đặt các phụ thuộc:
   ```bash
   mvn clean install
   ```
3. Chạy ứng dụng:
   ```bash
   mvn javafx:run
   ```

## 📂 Cấu trúc dự án sơ lược

- `com.fileoptimizer.core`: Logic xử lý lõi (quét, phân tích, quản lý plugin).
- `com.fileoptimizer.app.controller`: Điều hướng và xử lý sự kiện giao diện.
- `com.fileoptimizer.app.viewmodel`: Quản lý trạng thái và dữ liệu hiển thị.
- `com.fileoptimizer.ai`: Các tính năng liên quan đến trí tuệ nhân tạo.

## 🤝 Đóng góp

Mọi ý kiến đóng góp hoặc báo lỗi (issue) đều được hoan nghênh. Bạn có thể mở một Pull Request để cùng mình hoàn thiện dự án này.

---
**Phát triển bởi [Nguyễn Tuấn Kiệt (Yami)](https://github.com/Yami081204)**
*Dự án tốt nghiệp - File Optimizer*
