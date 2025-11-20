plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.kubhubsystem_gp13_dam"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.kubhubsystem_gp13_dam"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ Genera la constante BASE_URL dentro de BuildConfig
        buildConfigField("String", "BASE_URL", "\"http://54.91.220.71/\"")
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
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // ============================================================
    // CONFIGURACIÓN DE TESTING
    // ============================================================
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
    // ============================================================


    buildFeatures {
        buildConfig = true // 🔥 habilita la generación de BuildConfig
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirst("META-INF/kotlinx_coroutines_core.version")
        }
    }
}

dependencies {

    // ========================================
    //  COMPOSE & ANDROIDX CORE
    // ========================================
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.text)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.activity:activity-compose:1.9.3")

    // ---COMPOSE BOM ---
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // ---COMPOSE LIFECYCLE ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")

    // ---NAVERGATION ---
    implementation("androidx.navigation:navigation-compose:2.9.5")

    // ========================================
    //  ROOM
    // ========================================
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // ========================================
    //  COROUTINES
    // ========================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // ========================================
    //  NETWORKING (Retrofit + OkHttp + Gson)
    // ========================================
    // ---RETROFIT ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // ---OKHTTP ---
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ---GSON ---
    implementation("com.google.code.gson:gson:2.10.1")
    // ========================================
    //  IMAGES (COIL)
    // ========================================
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil:2.6.0")

    // ========================================
    //  GOOGLE PLAY SERVICES
    // ========================================
    implementation("com.google.android.gms:play-services-location:21.3.0")


    // ========================================
    //  ✅ TESTING CORE (NECESARIO)
    // ========================================
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0") // Para LiveData/ViewModel testing

    // ========================================
    //  ✅ MOCKK (NECESARIO para Repository y ViewModel tests)
    // ========================================
    testImplementation("io.mockk:mockk:1.13.8")
    // ❌ ELIMINAR mockk-android (NO necesario para unit tests)

    // ========================================
    //  ✅ COROUTINES TESTING (NECESARIO)
    // ========================================
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // ========================================
    //  ✅ MOCKWEBSERVER (NECESARIO para ApiService tests)
    // ========================================
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    // ❌ ELIMINAR esta línea (ya viene con mockwebserver):
    // testImplementation("com.squareup.okhttp3:okhttp:4.12.0")

    // ========================================
    //  ✅ GSON (NECESARIO para ApiService tests)
    // ========================================
    testImplementation("com.google.code.gson:gson:2.10.1")

    // ========================================
    //  ❌ ELIMINAR (NO usados en los tests proporcionados)
    // ========================================
    // testImplementation("com.squareup.retrofit2:retrofit:2.9.0")           // NO necesario
    // testImplementation("com.squareup.retrofit2:converter-gson:2.9.0")    // NO necesario
    // testImplementation("app.cash.turbine:turbine:1.0.0")                 // NO necesario
    // testImplementation("com.google.truth:truth:1.1.5")                   // NO necesario

    // ========================================
    //  ✅ ANDROID TESTING (Ya estaban bien)
    // ========================================
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // ========================================
    //  ✅ DEBUG TOOLS (Ya estaban bien)
    // ========================================
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ========================================
    //  ✅ TUS DEPENDENCIAS PRINCIPALES (Asegúrate de tenerlas)
    // ========================================
    // Estas deben estar en "implementation" (no en test)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation(kotlin("test"))

}
