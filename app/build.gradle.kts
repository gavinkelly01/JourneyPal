plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}
apply(plugin = "kotlin-kapt")

android {
    namespace = "com.example.journeypal"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.journeypal"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/DEPENDENCIES",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/LICENSE.txt"
            )
        }
    }
}

// Force use of protobuf-javalite and exclude protobuf-lite
configurations.all {
    resolutionStrategy {
        force("com.google.protobuf:protobuf-javalite:3.25.1")
    }
    // Exclude protobuf-lite from any dependency that could bring it in
    exclude(group = "com.google.protobuf", module = "protobuf-lite")
}

dependencies {
    // Image loading
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.ar.core)
    androidTestImplementation(libs.androidx.core.testing)
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // AndroidX
    implementation(libs.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.transition)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // CameraX
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")

    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    // Google Play Services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Google Maps Utils
    implementation("com.google.maps.android:android-maps-utils:2.3.0")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    // Firebase (exclude protobuf-lite)
    implementation("com.google.firebase:firebase-database-ktx:20.3.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    implementation("com.google.firebase:firebase-analytics-ktx:21.5.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    implementation("com.google.firebase:firebase-core:21.1.1") {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }

    // ML Kit
    implementation(libs.places)
    implementation("com.google.mlkit:text-recognition:16.0.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    implementation("com.google.mlkit:common:18.6.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }

    // UI extras
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.25")
    implementation("com.airbnb.android:lottie:6.0.0")

    // Encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // Other
    implementation("com.google.guava:guava:33.0.0-android")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation(project(":sdk"))

    // Debug/Test support
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")

    // Unit testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    testImplementation("org.mockito:mockito-core:4.6.1")
    testImplementation("org.mockito:mockito-inline:4.6.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation ("io.mockk:mockk:1.12.0")
    testImplementation ("io.mockk:mockk:1.13.10")
    testImplementation ("com.squareup.okhttp3:mockwebserver:4.9.1")
    androidTestImplementation ("io.mockk:mockk-android:1.13.10")

    // Instrumentation testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(libs.junit.junit)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.espresso.contrib)
}
