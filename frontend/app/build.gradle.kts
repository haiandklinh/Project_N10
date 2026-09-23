plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.attendanceapp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.attendanceapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["MAPS_API_KEY"] =
            project.findProperty("MAPS_API_KEY") ?: ""

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Hilt (Dependency Injection)
    implementation(libs.hilt.android)
    implementation(libs.play.services.maps)
    annotationProcessor(libs.hilt.compiler)

    //Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.room.rxjava3)


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.mapbox.maps:android:11.4.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")


    //Slider - Banner (Custom)
    implementation ("me.relex:circleindicator:2.1.6")

    // Core library desugaring: cho phép dùng java.time.* (LocalDate, LocalTime...)
    // trên các thiết bị Android dưới API 26 (minSdk 24 hiện tại của project)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")


    // Thư viện vẽ biểu đồ MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("androidx.core:core-splashscreen:1.0.1")


    // Retrofit cho việc gọi API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson Converter để chuyển JSON sang Java Object tự động
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // (Tùy chọn) Logging Interceptor giúp bạn xem log request/response ở Logcat khi debug
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Mã hóa dữ liệu lưu trữ SharedPreferences / File bảo mật
    implementation("androidx.security:security-crypto:1.1.0")

}