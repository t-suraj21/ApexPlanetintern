# FoodieGo 🍔

FoodieGo is a complete, modern food ordering Android application built from scratch using **Java + XML** in **Android Studio**. This project satisfies the requirements for **Android Development Internship Task 1**, establishing a solid foundation for a premium food delivery interface, responsive navigation, input validation, and dynamic lists.

---

## 📱 Features & User Flow

### 1. Splash Screen
*   **Visual Style:** Dynamic diagonal linear gradient background transitioning from Coral Orange (`#FF4F3C`) to Amber Gold (`#FFB300`).
*   **Gourmet Logo:** Custom premium vector drawable representing a serving cloche (plate lid) with steam and fast-delivery indicator lines.
*   **Transition:** Automatically redirects to the Login screen after a precise **2-second delay** using a Handler.
*   **Back Stack Control:** Calls `finish()` during redirect so the user cannot navigate backward into the Splash.

### 2. Login Screen
*   **Visual Style:** Soft-gray background with a elevated `MaterialCardView` layout container and modern outlined brand boxes.
*   **Outlined Inputs:** Outlined `TextInputLayout` containers with rounded edges and soft custom vector indicators.
*   **Robust Input Validation:**
    *   Verifies if the email field is empty.
    *   Verifies if the email matches standard formatting (e.g., `user@domain.com`).
    *   Verifies if the password is empty or under 6 characters.
*   **Dynamic Response:** Subscribes custom `TextWatcher` observers to inputs that proactively clear red error warnings as soon as the user starts correcting them.
*   **Navigation:** Launches the `HomeActivity` upon successful validation, or redirects to `RegisterActivity` when "Register here" is tapped.

### 3. Register Screen
*   **Form Capture:** Fully-featured registration form capturing **Full Name**, **Email**, **Password**, and **Confirm Password**.
*   **Validation Checks:**
    *   Checks for Name length (minimum 3 characters).
    *   Checks for standard Email formatting.
    *   Validates Password strength (minimum 6 characters).
    *   Conducts a **Confirm Password match validation** checking if both values are identical.
*   **Dynamic UX:** Automatically clears red warnings as the user types and clears the stack during home launch (`finishAffinity()`) so pressing the hardware back button from Home closes the app rather than returning to registration.

### 4. Home Screen Dashboard
*   **Premium App Bar:** Customized Toolbar featuring the brand logo, app title "FoodieGo", and a white vector avatar profile icon.
*   **Responsive Search Bar:** A fully responsive, rounded Material Card mockup search input displaying a search vector icon.
*   **Horizontal Categories List:**
    *   Populates Pizza, Burger, Pasta, Sandwich, Drinks, and Dessert categories.
    *   Leverages a custom `CategoryAdapter` with a **dynamic selection state highlighter**.
    *   When selected, card borders, icons, background tints, and text labels dynamically transition to high-contrast orange.
*   **Vertical Popular Foods List:**
    *   Populates **10 unique dummy food items** complete with prices, detailed descriptions, prices (₹), and "Add" action buttons.
    *   Uses a vertical `RecyclerView` backed by `FoodAdapter`.
    *   Integrates **Glide** image processing library for high-speed cache management, fading transition effects, and fallback placeholder states.

---

## 🛠️ Tech Stack & Design Tokens

*   **Language:** Java (JDK 8 / VERSION_1_8 compatibility)
*   **Layout Engine:** Android XML (ConstraintLayout, LinearLayout, ScrollView, NestedScrollView)
*   **UI System:** Google Material Design Components (`com.google.android.material`)
*   **List Rendering:** RecyclerView (Horizontal & Vertical)
*   **Image Loading:** Glide (v4.16.0)
*   **Pattern Architecture:** Android ViewBinding (100% type-safe views loading)
*   **Primary Color:** Coral Red (`#FF4F3C`)
*   **Accent Color:** Warm Amber (`#FFB300`)
*   **Background Color:** Soft Off-White (`#F8F9FA`)

---

## 📂 Project Structure

```text
com.foodiego
│
├── activities
│   ├── SplashActivity.java    # 2s delay, transitions to Login
│   ├── LoginActivity.java     # Email/Password validation, login action
│   ├── RegisterActivity.java  # Name, Email, Password, Confirmation validation
│   └── HomeActivity.java      # Dashboard container orchestrating lists
│
├── adapters
│   ├── CategoryAdapter.java   # Horizontal list adapter with dynamic selection
│   └── FoodAdapter.java       # Vertical food items card adapter with Glide
│
└── models
    └── Food.java              # Object model for food entities
```

```text
res
│
├── drawable
│   ├── bg_splash_gradient.xml # Diagonal Coral-to-Amber linear gradient
│   ├── bg_circle_icon.xml     # Soft light coral circle icon background
│   ├── ic_foodiego_logo.xml   # Custom vector logo (Gourmet cloche & motion lines)
│   ├── ic_pizza.xml           # Custom Vector Icon for Pizza
│   ├── ic_burger.xml          # Custom Vector Icon for Burger
│   ├── ic_pasta.xml           # Custom Vector Icon for Pasta
│   ├── ic_sandwich.xml        # Custom Vector Icon for Sandwich
│   ├── ic_drinks.xml          # Custom Vector Icon for Drinks
│   ├── ic_dessert.xml         # Custom Vector Icon for Cupcake Dessert
│   ├── ic_search.xml          # Custom Vector Icon for Search
│   ├── ic_profile.xml         # Custom Vector Icon for Toolbar User Profile
│   └── ic_shopping_cart.xml   # Custom Vector Icon for Shopping Cart
│
├── layout
│   ├── activity_splash.xml    # Full screen layout for Splash screen
│   ├── activity_login.xml     # Outlined login card layout
│   ├── activity_register.xml  # Multi-input registration card layout
│   ├── activity_home.xml      # Custom AppBar, search mock, categories & foods list
│   ├── item_category.xml      # Horizontal single-item layout card
│   └── item_food.xml          # Popular food single-item card layout (Unsplash + Price + Button)
│
└── values
    ├── colors.xml             # HSL Color Theme Tokens
    ├── strings.xml            # UI strings and labels
    └── themes.xml             # Material DayNight Theme configuration (NoActionBar)
```

---

## 🚀 Step-by-Step Setup Instructions

To open, build, and run the FoodieGo project in **Android Studio**, follow these instructions:

### Prerequisites
*   **Android Studio** installed (version Hedgehog 2023.1.1 or higher is recommended for smooth Gradle sync).
*   An Android Device or Android Virtual Device (AVD) running **Android 7.0 (API Level 24)** or higher.

### Step 1: Open the Project
1.  Launch **Android Studio**.
2.  On the Welcome dialog, click **Open** or **Import Project**.
3.  Browse and select the `Apexplanet` directory:
    `/Users/surajkumar/Developer/Apexplanet`
4.  Click **OK**. Android Studio will load the directory structure.

### Step 2: Gradle Sync & Dependency Build
1.  Android Studio will automatically detect the Gradle build environment using the provided files (`build.gradle`, `settings.gradle`, `gradle.properties`).
2.  The editor will start downloading dependencies, including the **Glide Image Processing library**.
3.  Wait for the build task to display **Gradle Sync Successful** in the bottom terminal.

### Step 3: Run the Application
1.  Ensure an Android Emulator (AVD) is running, or connect a physical Android device via USB debugging.
2.  In the top navigation toolbar, select the `app` run configuration.
3.  Click the green **Run** button (or press `Shift + F10`).
4.  Android Studio will compile and build the APK and launch it on your target device!

---

## 🍽️ Dummy Data Set (Included in Code)

The app launches with 10 high-quality dummy menu items pre-populated from Unsplash's professional food photography directory:

1.  **Pizza Margherita** (₹199) - Classic mozzarella, fresh basil, olive oil, and house marinara sauce.
2.  **Cheese Burger** (₹149) - Double-layered prime beef patty, cheddar cheese, fresh veggies, and secret house sauce.
3.  **White Sauce Pasta** (₹249) - Penne pasta tossed in rich, creamy parmesan cheese sauce with sauteed garlic mushrooms.
4.  **Veggie Club Sandwich** (₹119) - Triple decker toasted sandwich loaded with fresh cucumber, tomatoes, lettuce, and premium cheddar.
5.  **Caramel Macchiato** (₹179) - Rich espresso combined with milk and sweet vanilla syrup, finished with a caramel drizzle.
6.  **Double Chocolate Muffin** (₹99) - Rich, moist chocolate muffin filled with premium Belgian dark chocolate chips.
7.  **Paneer Tikka Wrap** (₹159) - Grilled spicy cottage cheese cubes wrapped in a soft tortilla wrap with crisp onions and mint mayo.
8.  **Garlic Bread with Cheese** (₹129) - Crispy freshly baked artisanal baguette slices topped with garlic herb butter and melted mozzarella.
9.  **Chocolate Lava Cake** (₹139) - Hot chocolate soufflé cake with a rich and gooey molten Belgian chocolate center.
10. **Tropical Mango Smoothie** (₹149) - A creamy and refreshing tropical blend of sweet Alphonso mangoes, fresh banana, and Greek yogurt.
