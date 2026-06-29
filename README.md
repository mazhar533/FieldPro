# FieldPro - Field Service Management Mobile Application

**Task ID**: MAD-2  
**Internship Program**: Teyzix Core Internship (June Batch)  
**Domain**: Mobile App Development  

---

## 📌 Project Overview
**FieldPro** is a native Android mobile application designed for field service technicians working remotely. It streamlines field service operations, making it easy to manage assigned service requests, track work progress, verify onsite attendance, and submit detailed reports.

For administrators, the app provides real-time tracking, job scheduling, re-assignment before acceptance, and detailed technician performance analytics charts. For technicians, it offers a clean layout to accept tasks, start work, record GPS coordinate points, attach service evidence photos, and scan onsite equipment QR codes.

The application is built completely using **Jetpack Compose** and **Material Design 3** guidelines, prioritizing clean architecture, responsive layouts, micro-animations, and a robust offline-first user experience.

---

## 🚀 Key Features

### 1. Authentication & Session Management
- Clean, secure login flow with email and password.
- Password validation and error handling.
- Forgot password email reset link dialog.
- Saved user session (remains logged in even after closing the app).
- In-app profile password update dialogue.

### 2. Live Dashboards (Daily Overview & Analytics)
- **Technician Side**: Welcome card greeting the logged-in technician, interactive stats grid (Active, Pending, Completed jobs), and an "Up Next" task highlight banner.
- **Admin Side**: Statistics charts (with infinite-translation shimmer skeletons during load) and interactive technician profile sheets.

### 3. Job Management & Timeline
- Filterable jobs listing tab (All, Completed, Pending, Active requests).
- Color-coded badges representing current status (`PENDING`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`).
- **Context-aware action buttons**:
  - `PENDING` $\rightarrow$ Accept Job / Reject Job options.
  - `ASSIGNED` $\rightarrow$ "Start Work" button.
  - `IN_PROGRESS` $\rightarrow$ "Create Service Report" & "Mark Completed" (locked until report is submitted).

### 4. Admin Edit & Re-assignment
- Admins can edit job details (Customer Name, Service Type, Issue, Address, Contact, Schedule Date/Time) and re-assign technicians *only if* the job has not been accepted yet (`PENDING`).

### 5. Customer Details & Service History
- Direct customer details view with address and contact info.
- **One-click Dialer Intent**: Instantly dial customer phone numbers.
- **One-click Google Maps Intent**: Locate customer addresses on Google Maps with an automatic browser fallback.
- **Customer Service History**: Dynamically matches the customer's phone number to list all past and present service requests. Clicking any item in the history navigates directly to that job's details.

### 6. Notifications & Alerts
- Local notification tray pushes for new job assignments and status changes.
- **Notification De-duplication**: Replaces older status logs for the same Task ID to maintain a clean alert history.
- **Scheduled Job Reminders**: Scans active tasks and triggers daily push notifications if there are jobs scheduled for today's system calendar date.

### 7. Real-Time Cloud Storage & Firebase Sync
- Fully integrated with **Firebase Firestore** for real-time synchronization.
- **Spark Plan Cloud Storage Bypass**: Downscales captured evidence photos to 640px max width and JPEG-serializes them directly into Firestore documents as compressed Base64 strings.

### 8. Live GPS Verification
- Integrated Google Play Services Location SDK to log coordinates.
- **Work Start Location**: Records coordinates when the technician starts work.
- **Work Completion Location**: Records coordinates when the report is submitted.
- Renders coordinate verification cards with direct deep link intents to drop a pin on Google Maps.

### 9. QR Code-Based Job Verification (Onsite Proof)
- Prevents service report submission until the onsite QR tag has been scanned.
- Displays an interactive camera viewfinder mockup dialog with glowing brackets and an animating laser scanline. Matches the code against the Job ID to mark verification as successful.

---

## 🛠️ Technology Stack
- **Programming Language**: Kotlin
- **UI Framework**: Jetpack Compose (Declarative UI)
- **Design Guidelines**: Google Material Design 3 (M3)
- **Cloud Database**: Google Firebase Firestore (Real-time sync)
- **Location Services**: Google Play Services Location SDK
- **Local Storage**: SharedPreferences with `org.json` Serialization
- **Architecture Pattern**: MVVM (Model-View-ViewModel) / Single Activity Architecture

---

## 📂 Project Structure
```text
com.mazhar.fieldpro/
│
├── MainActivity.kt        # App controller, navigation, and intent routing
│
├── data/
│   ├── Models.kt          # Data classes (User, ServiceRequest, AlertNotification)
│   └── Repository.kt      # SharedPreferences local storage manager & mock data
│
└── ui/
    ├── screens/
    │   ├── LoginScreen.kt              # Credentials flow
    │   ├── HomeScreen.kt               # Statistics & Up Next dashboard
    │   ├── JobsScreen.kt               # Filterable requests listing
    │   ├── JobDetailsScreen.kt         # Timeline & status-based action buttons
    │   ├── ServiceReportFormScreen.kt  # Text area inputs for report creation
    │   ├── AlertsScreen.kt             # Read/unread notification center
    │   ├── ProfileScreen.kt            # Avatar, role preferences & logout
    │   └── SplashScreen.kt             # Scale-up logo entrance transition
    │
    └── theme/
        ├── Color.kt       # Customized color palette (BluePrimary, RedPending, etc.)
        ├── Theme.kt       # Material 3 Theme setup (Locks light & dark properties)
        └── Type.kt        # Font typography settings
```

---

## 🔧 Installation & How to Build
1. Clone this repository to your local system.
2. Open the project in **Android Studio (Quail or newer)**.
3. Allow Gradle to sync dependencies and build index files.
4. Select your target device/emulator (requires SDK 26 / Android 8.0 or higher).
5. Run the app or build the APK via **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
