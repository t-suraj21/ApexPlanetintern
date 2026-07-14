# FoodieGo 🍔 - Food Ordering Android Application

FoodieGo is a professional, production-grade food ordering Android application built using **Java + XML** in **Android Studio**. This repository contains the finalized codebase for the **Android Development Internship Project**, delivering a premium user experience styled similarly to modern food delivery platforms like Zomato, Swiggy, and Uber Eats.

---

## 📱 Features & Components

### 1. In-App Notifications 🔔 [NEW]
*   **Notifications Log**: Tapping the notification bell launches a custom-built, modern `NotificationsBottomSheet` displaying all active system and simulated push alerts.
*   **Auto-Formatting**: Displays real-time elapsed timestamps (e.g., "Just now", "5 minutes ago", "2 hours ago").
*   **Log Purge**: Includes an option to clear notification logs dynamically.

### 2. Localization & Dark Mode [NEW]
*   **Hindi Localization**: Complete language switching support. When the user changes settings to Hindi, the app interface instantly updates with translated Hindi strings (`values-hi`).
*   **Sleek Night Theme**: Added premium dark mode styling (`values-night`), mapping backgrounds, texts, and borders to slate colors for maximum legibility.

### 3. Splash Screen
*   **Visual Style**: Coral-to-Amber linear diagonal gradient background.
*   **Launcher Logo**: Gourmet serving cloche vector logo with steam waves and fast-delivery motion indicators.
*   **Automatic Transition**: Redirects to Login/Home after a precise **2-second delay** with smooth fade transitions.

### 4. Registration & Login Flow
*   **Validation Checking**: Outlined `TextInputLayout` containers with floating icons, email regex checks, and password strength checks.
*   **Scalable Buttons**: Programmatic touch-scaling feedback on primary action buttons.
*   **Backstack Control**: Calls `finishAffinity()` on Home redirection to prevent backward navigation loops.

### 5. Home Dashboard
*   **Dashboard Filtering**: Active chip tags (Veg-Only, Popular, Fast Delivery, Price Ranks, Ratings) instantly update and filter item adapters.
*   **Horizontal Categories**: Categories (Pizza, Burger, Pasta, Sandwich, Drinks, Dessert) update primary highlights dynamically on selection.
*   **Double-Row Food Catalog**: Separate sections for Popular food choices (horizontal) and Recommended food listings (vertical scroll).

### 6. Food Details & Dynamic Cart
*   **Collapsible Images**: Clean collapsing details header using Unsplash photographs.
*   **Static Cart Singleton**: `CartManager` coordinates add/remove adjustments, billing sum values, and delivery charges.
*   **Bill Breakdown**: Subtotals, CGST restaurant taxes (₹18), and delivery charges (₹30) refresh cart payment panels in real-time.

### 7. Timeline Order Tracking
*   **Steppers Stepping**: Visual tracker timeline tracing active order stages:
    `Placed ➔ Preparing ➔ Out for Delivery ➔ Delivered`
*   **Status Simulator**: Simulated status steps that dispatch corresponding local notifications.

---

## 📂 Project Structure

```text
com.foodiego
│
├── activities
│   ├── AddressActivity.java         # Add, edit, remove delivery address
│   ├── CheckoutActivity.java        # Form inputs validation, bill sums, pay card forms
│   ├── FavoritesActivity.java       # User favorite food item lists
│   ├── FoodDetailsActivity.java     # Image viewer header, quantity local counter
│   ├── HomeActivity.java            # BottomNavigationView tab controller (Home, Cart, Orders, Profile)
│   ├── LoginActivity.java           # Authentication, text checkers
│   ├── NotificationsBottomSheet.java# Bottom sheet displaying logged user notifications
│   ├── OrderTrackingActivity.java   # Stepper progress timeline tracking
│   ├── RegisterActivity.java        # Registration forms cards
│   ├── SettingsActivity.java        # Language switcher, night theme switches, policy info
│   └── SplashActivity.java          # 2s delay diagnostic loader
│
├── adapters
│   ├── AddressAdapter.java          # List address cards
│   ├── CartAdapter.java             # Quantity +/- controls and removals
│   ├── CategoryAdapter.java         # Category items highlights
│   ├── FoodAdapter.java             # Food lists
│   ├── OrderAdapter.java            # Order lists
│   ├── PopularFoodAdapter.java      # Popular food cards
│   └── RecommendedFoodAdapter.java  # Vertical recommended items recycler
│
├── firebase
│   ├── AuthManager.java             # FirebaseAuth bindings
│   ├── FirebaseHelper.java          # Profile images upload, firestore favorites
│   └── FirestoreManager.java        # User collections CRUD operations
│
├── models
│   ├── Address.java                 # Address model
│   ├── CartItem.java                # Cart selection quantities model
│   ├── Food.java                    # Food rating, delivery duration model
│   ├── NotificationItem.java        # Saved notification message model
│   ├── Order.java                   # Order status, totals model
│   └── User.java                    # Profile details model
│
├── network
│   ├── ApiService.java              # Retrofit routing paths interface
│   ├── CartSyncBody.java            # Sync REST endpoints models
│   ├── Repository.java              # REST API calls controller
│   └── RetrofitClient.java          # OkHttp configurations
│
├── services
│   └── MyFirebaseMessagingService.java# Firebase Cloud Messaging & notification channels
│
└── utils
    ├── CartManager.java             # Static cart repository singleton
    ├── NotificationHelper.java      # SharedPreferences notifications logger
    ├── OfflineCacheManager.java     # Local backup memory manager
    └── SessionManager.java          # App login preferences
```

---

## 🛠️ Step-by-Step Installation & Local Run

### 1. Run Node.js API Backend
Ensure you have **Node.js** and **MongoDB** installed and running on your host machine.
```bash
# Navigate to the API directory
cd foodiego_api

# Install backend dependencies
npm install

# Start the REST API server (runs on port 8001)
npm start
```

### 2. Configure Android SDK & Boot Emulator
You can manage devices via Android Studio or compile directly using the terminal.
```bash
# Start your Android emulator (e.g. Pixel_9_Pro)
android emulator start Pixel_9_Pro
```

### 3. Build & Run Android Application
Run Gradle tasks to deploy the debug application.
```bash
# Verify device connection
adb devices

# Compile and install on running emulator
./gradlew installDebug
```

---

## 📦 Compiling Production Release Build

To compile a signed production APK prepared for Google Play Store upload:
```bash
# Clean project build caches
./gradlew clean

# Compile signed Release APK
./gradlew assembleRelease
```
The output signed APK `app-release.apk` is saved in:
`app/build/outputs/apk/release/app-release.apk`

---

## 🔮 Future Scope
*   **Live Map Tracking**: Integrate Google Maps API to visualize riders in real-time.
*   **Payment Gateways**: Incorporate Razorpay or Stripe SDKs for live transactions.
*   **Personalization**: Implement food recommendations using machine learning.
