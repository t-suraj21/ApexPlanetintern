# FoodieGo 🍔 - Task 2: UI/UX Design & Implementation

FoodieGo is a professional food ordering Android application built using **Java + XML** in **Android Studio**. This repository contains the complete codebase for **Android Development Internship Task 2**, transforming the application into a highly premium, production-quality food delivery app with styling and flows similar to Zomato, Swiggy, and Uber Eats.

---

## 📱 Features & Screens

### 1. Splash Screen
*   **Visual Style:** Coral-to-Amber linear diagonal gradient background.
*   **Launcher Logo:** Displays a serving cloche (plate lid) vector logo with steam waves and fast-delivery indicators.
*   **Transition:** Automatically redirects to the Login screen after a precise **2-second delay** with smooth fade animations.

### 2. Login & Forgot Password Screen
*   **Outlined Inputs:** Modern rounded outlined `TextInputLayout` containers with floating icons and validation-ready fields.
*   **Forgot Password Link:** Embedded "Forgot Password?" clickable text link displaying interactive toast confirmations.
*   **Micro-Animations:** Programmatic button scaling (scale down on touch down, scale up on release) providing rich tactile feedback.
*   **Input Validation:** Email syntax checking (standard regex format matching) and password length checking (minimum 6 characters).

### 3. Register Screen
*   **Comprehensive Capture:** Gathers Full Name, Email, Password, and Confirm Password inside a unified card layout.
*   **Form Validation:** Checks name length (minimum 3 characters), email formatting, password strength, and performs a matching check on "Confirm Password".
*   **Navigation Stack Control:** Automatically clears background task stacks on Home launch (`finishAffinity()`) to prevent standard hardware back navigations from landing on login sheets.

### 4. Home Screen Dashboard
*   **Toolbar Headings:** Custom top bar displaying greeting banners ("Hello, Rahul 👋"), tagline prompts, and notification vectors.
*   **Responsive Search Bar:** Rounded solid white mock input bar (`@drawable/bg_search_bar`) displaying active search vector icons.
*   **Horizontal Categories List:**
    *   Horizontal scroll area populated with Pizza, Burger, Pasta, Sandwich, Drinks, and Dessert categories.
    *   Dynamic selected highlights: tapping a category dynamically updates and fills cards with warm primary orange overlays.
*   **Horizontal Popular Foods List:**
    *   Displays high-rating food items, delivery duration text (⏱), and circular star ratings (★).
    *   Tapping cards triggers sliding transitions to open the Food Details page.
*   **Vertical Recommended Foods List:**
    *   Displays 10 rich local food products with descriptions, Unsplash photographic thumbnails loaded via Glide, and active add actions.

### 5. Collapsible Food Details Screen
*   **collapsing Image Header:** Large Unsplash product photography with custom back chevron overlays.
*   **Product Information Panel:** Renders food names, price lines, green ratings tags, and descriptions.
*   **Quantity Picker Panel:** A horizontal click counter allowing users to increase or decrease quantities (+ / -) locally.
*   **Primary Action:** A solid button triggering adding the product and selected quantity to the local shopping cart.

### 6. Dynamic Cart Screen
*   **Item Listing Recycler:** Displays all added products with Unsplash thumbnails, price labels, and quantity controls.
*   **Real-time Quantity Controls:** Tap `+` or `-` inside the cart to adjust counts. If counts drop to 0, the item is dynamically removed from the list.
*   **Bill Details Breakdown:** Sums item subtotals, restaurant taxes (₹18), and delivery charges (₹30), refreshing billing Pay cards in real time.
*   **Empty State Layout:** Displays a stylized empty checkout vector and browse redirects when the cart is cleared.

---

## 🛠️ Tech Stack & Design Tokens

*   **Language:** Java (JDK 8 / VERSION_1_8 compatibility)
*   **Layout Engine:** XML Layouts (ConstraintLayout, LinearLayout, ScrollView, NestedScrollView)
*   **UI System:** Material Design 3 Guidelines (`com.google.android.material`)
*   **Image Processing:** Glide (v4.16.0)
*   **List Rendering:** RecyclerView (Categories, Popular Foods, Recommended Foods, Cart Items)
*   **Pattern Architecture:** Android ViewBinding (100% type-safe views loading)
*   **Primary Color (Coral Orange):** `#FF6B35`
*   **Secondary Color (Amber Gold):** `#FF9F1C`
*   **Background (Soft warm off-white):** `#F8F9FA`
*   **Text (Slate black):** `#212529`
*   **Accent Red:** `#FF4D4D`

---

## 📂 Project Structure

```text
com.foodiego
│
├── activities
│   ├── SplashActivity.java       # Automatic 2s transition timer
│   ├── LoginActivity.java        # Email/Password validation, forgot password
│   ├── RegisterActivity.java     # Full name, email, confirm password match
│   ├── HomeActivity.java         # Main dashboard, BottomNavigationView listener
│   ├── FoodDetailsActivity.java  # Collaborated details viewer, item counters
│   └── CartActivity.java         # Scroll items list, real-time bill calculations
│
├── adapters
│   ├── CategoryAdapter.java      # Horizontal category list with selections
│   ├── PopularFoodAdapter.java   # Horizontal popular foods list with intent links
│   ├── RecommendedFoodAdapter.java # Vertical recommended foods card list
│   └── CartAdapter.java          # Cart items list, count triggers, item removals
│
├── models
│   ├── Food.java                 # Food object (rating, deliveryTime, price)
│   ├── Category.java             # Category object (name, iconResId)
│   └── CartItem.java             # Cart selection object (food, quantity)
│
└── utils
    └── CartManager.java          # Real-time static cart repository singleton
```

```text
res
│
├── anim
│   ├── slide_in_right.xml        # Slides transition in from right edge
│   ├── slide_out_left.xml        # Slides transition out to left edge
│   ├── slide_in_left.xml         # Slides back in from left edge
│   └── slide_out_right.xml       # Slides back out to right edge
│
├── drawable
│   ├── bg_splash_gradient.xml    # Diagonal linear primary gradient
│   ├── bg_button_gradient.xml    # Rounded orange-to-gold button shape
│   ├── bg_search_bar.xml         # White solid search layout with borders
│   ├── bg_circle_icon.xml        # Soft circular category overlay
│   ├── ic_foodiego_logo.xml      # Brand Vector Logo (Gourmet cloche & speed lines)
│   ├── ic_home.xml               # Vector icon for bottom navigation (home)
│   ├── ic_cart.xml               # Vector icon for bottom navigation (cart)
│   ├── ic_orders.xml             # Vector icon for bottom navigation (orders)
│   ├── ic_notification.xml        # Vector icon for toolbar notification alert
│   ├── ic_pizza.xml / ic_burger.xml / ic_pasta.xml / ic_sandwich.xml / ic_drinks.xml / ic_dessert.xml
│   └── ic_search.xml / ic_profile.xml
│
├── layout
│   ├── activity_splash.xml       # Centered logo, name, tagline, loading overlay
│   ├── activity_login.xml        # Outlined inputs with Forgot Password
│   ├── activity_register.xml     # Registration card with multiple text fields
│   ├── activity_home.xml         # Custom app bar, search bar, list headers, BottomNavigationView
│   ├── activity_food_details.xml # COLLAPSING header image, descriptives, picker, Add button
│   ├── activity_cart.xml         # Cart scroll recycler, bill panel, Place Order action
│   ├── item_category.xml         # Category vertical rounded single card
│   ├── item_popular_food.xml     # Popular horizontal layout card with badges
│   ├── item_food.xml             # Recommended vertical card with Unsplash
│   └── item_cart_food.xml        # Cart horizontal card with quantity indicators
│
└── values
    ├── colors.xml                # M3 styling color codes
    ├── strings.xml               # UI text labels
    └── themes.xml                # Material NoActionBar themes
```

---

## 🚀 Step-by-Step Setup Instructions

To open, build, and run the FoodieGo project in **Android Studio**:

1.  **Open the Workspace**: Open Android Studio, click **Open** or **Import**, and select `/Users/surajkumar/Developer/Apexplanet`.
2.  **Allow Gradle Sync**: Android Studio will automatically resolve references, download the **Glide** library, and complete the build.
3.  **Compile & Run**: Ensure your AVD Emulator is started (or connect a debug phone), select `app` in the top bar, and click **Run** (or `Shift + F10`).
4.  **Terminal Build Option**: Open the integrated terminal in Android Studio or VS Code and run `./gradlew assembleDebug` to compile and output `app-debug.apk`.
