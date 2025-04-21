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
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.androidx.core)
    implementation(libs.androidx.core)
    implementation(libs.androidx.transition)
    implementation(libs.places)
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.firebase.database.ktx)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("androidx.security:security-crypto:1.0.0")
    implementation ("org.mindrot:jbcrypt:0.4")
    implementation ("com.airbnb.android:lottie:5.0.3")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.25")
   implementation ("androidx.camera:camera-core:1.2.0")
    implementation ("androidx.camera:camera-camera2:1.2.0")
    implementation ("androidx.camera:camera-lifecycle:1.2.0")
    implementation ("androidx.camera:camera-view:1.0.0-alpha04")
    implementation ("com.google.guava:guava:31.1-android")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.camera:camera-camera2:1.1.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.camera:camera-lifecycle:1.1.0")
    implementation ("androidx.camera:camera-view:1.0.0")
    implementation ("com.github.bumptech.glide:glide:4.13.2")
    implementation(libs.firebase.inappmessaging)
    implementation ("com.google.android.material:material:1.6.1")
    implementation ("androidx.core:core-ktx:1.10.0")
    implementation ("org.tensorflow:tensorflow-lite-task-vision:0.4.0")
    implementation ("org.tensorflow:tensorflow-lite:2.8.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.3.1")
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.core)
    implementation(project(":sdk"))
    implementation(libs.core)
    implementation ("com.google.maps.android:android-maps-utils:2.2.0")
    implementation ("com.google.android.gms:play-services-location:18.0.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.2")
    implementation("com.google.android.gms:play-services-location:18.0.0")
    implementation ("org.tensorflow:tensorflow-lite:2.8.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.3.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.1")
    implementation("com.google.android.gms:play-services-maps:18.0.2")
    implementation(libs.androidx.ui.text.android)
    implementation ("com.google.firebase:firebase-database-ktx:20.0.0")
    implementation ("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation ("com.google.firebase:firebase-core:20.0.0")
    implementation ("com.google.firebase:firebase-analytics-ktx:20.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
