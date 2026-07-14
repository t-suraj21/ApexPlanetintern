# FoodieGo - Production Release Deployment Checklist

Follow this systematic checklist to prepare, test, compile, and upload **FoodieGo** to the Google Play Store.

---

## 🛠️ Phase 1: Pre-Release Build Preparation

*   `[ ]` **Update API Configurations**: Verify `RetrofitClient.java` contains the correct production endpoint. For local/development, use `http://10.0.2.2:8001/`; for staging/production, swap with the domain URL.
*   `[ ]` **Increment Versioning Variables**: Verify `defaultConfig` variables in `app/build.gradle`:
    *   `versionCode`: Increment value (e.g., from `1` to `2`).
    *   `versionName`: Set matching user version string (e.g., `"1.1.0"`).
*   `[ ]` **Verify Release Keystore**:
    *   Ensure the generated `release.keystore` is located securely inside the `/app` folder.
    *   Confirm variables in `build.gradle`'s `signingConfigs.release` match the keystore passwords and alias:
        *   `storePassword`: `"foodiego123"`
        *   `keyAlias`: `"foodiego_key"`
        *   `keyPassword`: `"foodiego123"`
*   `[ ]` **Enable Proguard Rules**:
    *   Set `minifyEnabled true` inside the release build type in `app/build.gradle`.
    *   Verify `proguard-rules.pro` has rules to preserve GSON models and Retrofit interfaces from obfuscation.

---

## 🧪 Phase 2: Compilation & Validation

*   `[ ]` **Clean Build Directory**: Clean old caches to prevent asset mismatches:
    ```bash
    ./gradlew clean
    ```
*   `[ ]` **Compile Signed Debug APK**: Verify compile-time warnings:
    ```bash
    ./gradlew assembleDebug
    ```
*   `[ ]` **Compile Signed Production APK**: Build the production release APK:
    ```bash
    ./gradlew assembleRelease
    ```
*   `[ ]` **Verify Artifact Output**:
    *   Verify `app-release.apk` is generated under `app/build/outputs/apk/release/`.
    *   Verify it is successfully signed. Use the apksigner verification tool:
        ```bash
        apksigner verify --verbose app/build/outputs/apk/release/app-release.apk
        ```

---

## 📤 Phase 3: Play Console Upload & Submission

*   `[ ]` **Generate Android App Bundle (AAB)**: If uploading a bundle instead of an APK:
    ```bash
    ./gradlew bundleRelease
    ```
*   `[ ]` **Set Store Listing details**:
    *   App Title: `FoodieGo`
    *   Short Description: (from `play_store_assets.md`)
    *   Full Description: (from `play_store_assets.md`)
*   `[ ]` **Upload Graphical Assets**:
    *   App Icon: `play_store_icon.png` (512x512)
    *   Feature Graphic: `feature_graphic.png` (1024x500)
    *   Upload vertical phone screenshots.
*   `[ ]` **Upload Signed Release Bundle**:
    *   Drag and drop the AAB/APK output from `app/build/outputs/bundle/release/app-release.aab`.
    *   Draft the release notes.
*   `[ ]` **Content Rating & Privacy Policy**:
    *   Complete the content rating questionnaire.
    *   Provide the Privacy Policy link referencing the text in `play_store_assets.md`.
*   `[ ]` **Submit to Closed Testing / Production Track**: Submit the release for review!
