# 🎓 UniPlan – Ứng Dụng Quản Lý Thời Gian Cho Sinh Viên (Android)

**UniPlan** là ứng dụng di động Android Native hỗ trợ sinh viên quản lý thời gian học tập, thời khóa biểu, hạn nộp bài (deadline), lịch thi và xây dựng thói quen tập trung hiệu quả. Ứng dụng hoạt động **hoàn toàn offline**, bảo mật thông tin cá nhân và tích hợp tính năng **Lọ thành tựu (Coin Jar)** độc đáo để tạo động lực hoàn thành mục tiêu.

---

## ✨ Chức Năng Chính (Key Features)

- 📅 **Lịch Học & Quản Lý Tiết Học**:
  - Thiết lập học kỳ, khung giờ tiết (hỗ trợ 1–16 tiết), môn học và mã môn.
  - Quản lý buổi học lặp hằng tuần (tuần chẵn, tuần lẻ, tất cả các tuần).
  - Xử lý ngoại lệ từng ngày: Nghỉ học, Học bù, Đổi phòng, Đổi giờ học.
  - Thẻ đếm ngược thông minh đến tiết học kế tiếp ngay ở màn hình Hôm nay.

- ⏰ **Thời Gian Biểu & Timeline Ngày Hợp Nhất**:
  - Hợp nhất buổi học, khối thời gian cá nhân (tự học, làm thêm, nghỉ ngơi...), lịch thi và deadline trên cùng một trục thời gian.
  - Thuật toán xếp cột tự động chia không gian cho các khối chồng giờ.

- ✅ **Quản Lý Nhiệm Vụ & Deadline (Ma Trận Eisenhower)**:
  - Thêm nhiệm vụ, việc con (subtasks), mức độ ưu tiên, thời gian ước lượng và môn học liên kết.
  - Tự động phân loại 4 nhóm ưu tiên: *Làm ngay*, *Lên kế hoạch*, *Xử lý nhanh*, *Để sau*.
  - Sắp xếp và tìm kiếm thông minh theo chuỗi tiếng Việt không dấu.

- 🔔 **Hệ Thống Báo Thức & Nhắc Nhở Chính Xác**:
  - Đăng ký báo thức chính xác với `AlarmManager` theo cửa sổ trượt 7 ngày.
  - Hỗ trợ giờ Không làm phiền (DND) im lặng thông báo.
  - Nút hành động trực tiếp trên thông báo: **"Xong"** (hoàn thành) hoặc **"Hoãn 10 phút"** (snooze).
  - Tự động khôi phục báo thức sau khi khởi động lại điện thoại.

- 🏺 **Lọ Thành Tựu (Coin Jar - Gamification)**:
  - Mỗi nhiệm vụ hoàn thành sẽ sinh ra một đồng xu (Đồng, Bạc, Vàng) thả vào chiếc lọ thủy tinh của tháng đó.
  - Hoàn thành phiên tập trung $\ge$ 25 phút nhận xu Tập trung (Focus Coin).
  - Vẽ bằng **Custom Canvas View** cùng thuật toán xếp xu vật lý cố định theo seed (`JarLayout`) và hiệu ứng xu rơi sinh động.

- ⏱️ **Đồng Hồ Tập Trung (Pomodoro)**:
  - Đếm ngược thời gian tập trung (25, 45, 60 phút...), giữ màn hình luôn sáng (`FLAG_KEEP_SCREEN_ON`).
  - Ghi nhận chính xác số phút tập trung thực tế vào CSDL.

- 📊 **Thống Kê Hiệu Suất**:
  - Biểu đồ trực quan theo tuần/tháng/học kỳ qua thư viện `MPAndroidChart`.
  - Đánh giá tỉ lệ hoàn thành công việc đúng hạn và tổng số giờ tập trung.

- 💾 **Sao Lưu & Khôi Phục Dữ Liệu**:
  - Xuất/nhập file cấu hình JSON hoàn toàn offline giúp chuyển đổi dữ liệu dễ dàng giữa các thiết bị.

---

## 🛠️ Kiến Trúc & Công Nghệ (Tech Stack)

| Thành phần | Công nghệ / Thư viện |
| :--- | :--- |
| **Ngôn ngữ** | Kotlin |
| **Hệ điều hành** | Android 8.0+ (API 26+) \| Target SDK 37 |
| **Kiến trúc** | MVVM + Repository Pattern + Clean Domain UseCases |
| **Giao diện (UI)** | Material Design 3, ViewBinding, Navigation Component, Custom Canvas View |
| **Cơ sở dữ liệu** | Room Database (SQLite) + TypeConverters |
| **Cấu hình** | Jetpack DataStore (Preferences) + Gson |
| **Báo thức & Nền** | AlarmManager, BroadcastReceiver, WorkManager |
| **Biểu đồ** | MPAndroidChart |
| **Bất đồng bộ** | Kotlin Coroutines + Flow / StateFlow |
| **Kiểm thử** | JUnit 4 |

---

## 📁 Cấu Trúc Gói (Package Structure)

```text
com.uniplan.app
├── UniPlanApp.kt            # Application Class & Notification Channels
├── data/
│   ├── local/db/           # Room Database, Entities, DAOs, Converters
│   ├── local/prefs/        # DataStore (Settings & PeriodConfig)
│   └── repository/         # Repositories (Semester, Task, Schedule, CoinJar...)
├── domain/
│   ├── model/              # Domain Enums & Models
│   └── usecase/            # Business Logic (OccurrenceGenerator, CheckConflict, JarLayout...)
├── notification/           # AlarmManager Scheduler & Receivers
├── backup/                 # JSON Backup & Import Engine
├── ui/                     # ViewModels, Fragments & Custom Views (CoinJarView)
└── util/                   # Date/Time Providers & Vietnamese Text Formatters
```

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Ứng Dụng (Getting Started)

### Yêu cầu hệ thống:
- Android Studio 2024.1+ (Koala / Ladybug trở lên).
- JDK 17+.
- Thiết bị hoặc máy ảo Android 8.0 (API 26) trở lên.

### Các bước thực hiện:

1. **Clone repository về máy**:
   ```bash
   git clone https://github.com/phananhh2805/UniPlan.git
   ```
2. **Mở dự án trong Android Studio**:
   - Chọn `Open` và trỏ tới thư mục vừa clone.
   - Chờ Android Studio đồng bộ Gradle (`Sync Project with Gradle Files`).

3. **Chạy ứng dụng**:
   - Chọn thiết bị máy ảo (Emulator) hoặc máy thật đã bật USB Debugging.
   - Nhấn nút **Run (Shift + F10)**.

---

## 👨‍💻 Nhóm Tác Giả (Authors)

- **Trịnh Quốc Toản** (Nhóm trưởng)
- **Chu Ngọc Giang**
- **Đoàn Phan Anh** ([@phananhh2805](https://github.com/phananhh2805))

---

## 📄 Giấy Phép (License)

Dự án này được phát hành dưới bản quyền tự do phục vụ mục đích học tập và nghiên cứu.
