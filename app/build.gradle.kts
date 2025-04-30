plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.junit.junit)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.junit.ktx)
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.transition:transition:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    implementation("com.google.maps.android:android-maps-utils:2.3.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    implementation("com.google.guava:guava:33.0.0-android")

    implementation("org.mindrot:jbcrypt:0.4")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.25")
    implementation("com.airbnb.android:lottie:6.0.0")

    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation(libs.places)
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.core.ktx)
    implementation(libs.core)
    implementation(libs.androidx.core)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.ui.text.android)
    implementation(project(":sdk"))

    // Unit testing
    testImplementation ("org.mockito:mockito-core:4.6.1")
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation ("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    testImplementation ("androidx.test:core:1.4.0")
    testImplementation ("androidx.arch.core:core-testing:2.1.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.robolectric:robolectric:4.12.0")
    testImplementation ("org.mockito:mockito-core:4.6.1")
    testImplementation ("org.mockito:mockito-inline:4.6.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Android Instrumentation testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
