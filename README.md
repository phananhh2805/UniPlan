# 🎓 UniPlan – Smart Academic & Time Management App for Students

**UniPlan** là ứng dụng quản lý thời gian và học tập thông minh dành cho sinh viên Android, tích hợp phong cách giao diện trực quan độc đáo truyền cảm hứng từ **Tiimo** (Soft Pastel Theme, Visual Time Tracking, Mood Check-in, Coin Jar Gamification).

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-blue.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-orange.svg)](https://developer.android.com/topic/architecture)

---

## ✨ Tính năng nổi bật & Giao diện Tiimo UX/UI

### 1. 🎨 **Thiết kế Tiimo UX/UI Pastel Dịu Mắt**
* **Bảng màu Soft Pastel**: Tone màu dịu mát (Soft Coral, Teal, Lavender, Mint, Peach) giảm mỏi mắt và áp lực tâm lý khi lập kế hoạch.
* **Hình khối Bo tròn (Tiimo Shapes)**: Thẻ và nút bấm bo góc lớn mềm mại ($20\text{dp} - 24\text{dp}$).

### 2. ⚡ **Ghi nhận Năng lượng & Tâm trạng (Energy & Mood Check-in)**
* Tích hợp thanh theo dõi trạng thái năng lượng ngay đầu ngày (*🔥 Sung sức, 🎯 Tập trung, 😐 Bình thường, ☕ Cần nghỉ ngơi*) giúp tự điều chỉnh khối lượng công việc.

### 3. ⏱ **Trực quan hóa Thời gian (Visual Time Tracking)**
* **Visual Elapsing Time Circle**: Thẻ tiết học/hoạt động đang diễn ra tích hợp vòng tròn tiến độ chạy thực tế ("Rendez le temps visible") giúp nhìn thấy thời gian đang trôi mà không cần nhẩm tính.
* **Next Class Countdown**: Tự động tính toán và hiển thị đếm ngược đến tiết học tiếp theo.

### 4. 🏺 **Lọ thành tựu (Coin Jar Gamification)**
* Mỗi khi hoàn thành một nhiệm vụ hoặc phiên tập trung, người dùng nhận được 1 đồng xu (Đồng, Bạc, Vàng, Focus) thả vào **Lọ thành tựu**.
* **Canvas Glass Jar Custom View**: Vẽ chiếc lọ thủy tinh bằng Canvas với thuật toán xếp xu vật lý tĩnh không chồng chéo.
* Theo dõi mục tiêu xu hàng tháng và giữ vững động lực tích cực.

### 5. 📅 **Lịch học & Thời gian biểu Linh hoạt**
* Hỗ trợ 3 chế độ xem: **Tuần**, **Ngày (Timeline chia cột)** và **Tháng**.
* Quản lý quy tắc lặp theo tuần (Tất cả, Tuần lẻ, Tuần chẵn) và ngoại lệ từng buổi (Nghỉ, Học bù, Đổi phòng, Đổi giờ).
* Tự động phát hiện trùng lịch học với thuật toán $O(N \times \text{số tuần})$.

### 6. 📋 **Nhiệm vụ & Ma trận Ưu tiên (Eisenhower Matrix)**
* Phân loại tự động 4 nhóm: *Làm ngay, Lên kế hoạch, Xử lý nhanh, Để sau*.
* Quản lý danh sách việc con (Subtasks), tiến độ và hạn nộp (Deadline).

### 7. ⏱ **Đồng hồ Tập trung (Focus Timer)**
* Bộ đếm giờ tập trung (25m, 45m, 60m) giữ màn hình sáng và tự động thưởng xu Focus khi hoàn thành phiên từ 25 phút.

### 8. 🔔 **Nhắc nhở & Báo thức Chính xác**
* Đăng ký báo thức qua `AlarmManager` (cửa sổ trượt 7 ngày).
* Tự động khôi phục báo thức sau khi khởi động lại máy (`BootReceiver`).
* Hỗ trợ giờ không làm phiền (DND Quiet Hours).

### 9. 💾 **Sao lưu & Khôi phục (Backup & Restore)**
* Xuất/Nhập dữ liệu toàn bộ ứng dụng bằng tệp JSON trong một giao dịch an toàn (Transaction).

---

## 🛠 Công nghệ & Kiến trúc

* **Ngôn ngữ**: Kotlin (100%)
* **Giao diện (UI)**: Jetpack Compose + Material Design 3
* **Kiến trúc**: MVVM + Repository Pattern + Clean Architecture UseCases
* **Cơ sở dữ liệu**: Room Database (13 Entities, DAOs, TypeConverters)
* **Quản lý Cài đặt**: Jetpack DataStore Preferences
* **Dependency Injection**: Google Hilt (Dagger)
* **Định thời**: AlarmManager, BroadcastReceivers, WorkManager
* **Bất đồng bộ**: Kotlin Coroutines & StateFlow / Flow

---

## 📱 Cấu trúc Dự án

```
com.uniplan.app/
├── UniPlanApp.kt               # Application Class (@HiltAndroidApp)
├── data/
│   ├── local/db/              # Room Database, Entities, DAOs, Converters
│   ├── local/prefs/           # SettingsDataStore, PeriodConfigStore
│   └── repository/            # Repositories (Semester, Subject, Schedule, Task, Exam, Focus, CoinJar,...)
├── domain/
│   ├── model/                 # Enums, Occurrence, TimelineItem
│   └── usecase/               # OccurrenceGenerator, CheckConflictUseCase, GroupTasksByPriorityUseCase,...
├── notification/              # ReminderScheduler, AlarmReceiver, BootReceiver, NotificationFactory
├── backup/                    # BackupManager, BackupDto
├── di/                        # Hilt Database & Utility Modules
├── util/                      # DateTimeProvider, WeekCalculator, VietnameseText
└── ui/
    ├── main/                  # MainActivity, Navigation Host & BottomBar
    ├── onboarding/            # Onboarding 3 bước
    ├── today/                 # Màn hình Hôm nay (Mood bar, Next class, Mini Jar)
    ├── schedule/              # Màn hình Lịch (Tuần/Ngày/Tháng & ClassForm)
    ├── tasks/                 # Màn hình Nhiệm vụ (List/Matrix & TaskForm & ExamForm)
    ├── focus/                 # Đồng hồ Tập trung
    ├── coinjar/               # Lọ thành tựu (Canvas Glass Jar & Sheet)
    ├── stats/                 # Thống kê
    ├── settings/              # Cài đặt, Quản lý học kỳ, Môn học, Khung tiết, Báo thức, Sao lưu
    └── theme/                 # Color, Theme, Type (Tiimo Pastel Palette)
```

---

## 🚀 Hướng dẫn Cài đặt & Biên dịch

1. **Yêu cầu hệ thống**:
   - Android Studio Ladybug (2024.2.1) trở lên
   - JDK 17
   - Android SDK 37 (Min API 26 - Android 8.0+)

2. **Clone Repository**:
   ```bash
   git clone https://github.com/phananhh2805/UniPlan.git
   cd UniPlan
   ```

3. **Biên dịch & Chạy dự án**:
   - Mở dự án trong Android Studio.
   - Nhấn **Sync Project with Gradle Files**.
   - Chọn thiết bị/giả lập Android (API 26 trở lên) và nhấn **Run (`Shift + F10`)**.

---

## 📜 Giấy phép & Tác giả

* **Nhóm phát triển**: Trịnh Quốc Toản (Nhóm trưởng), Chu Ngọc Giang, Đoàn Phan Anh (`phananhh2805`)
* **Phiên bản**: 1.0 (MVP Release)
