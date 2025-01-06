import com.android.build.api.dsl.ViewBinding

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.changli_planet_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.changli_planet_app"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding {
        enable = true
    }
    packaging {
        resources {
            // 排除重复的文件
            excludes += "mozilla/public-suffix-list.txt"
        }
    }

}
dependencies {

    // Material Design
    implementation("com.google.android.material:material:1.11.0")
    // Blurry库
    implementation(files("libs/blurry-4.0.1.aar"))
    //RxJava
    implementation("io.reactivex.rxjava3:rxjava:3.1.9")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation(files("libs/room-rxjava3.aar"))
    implementation(files("libs/rxjava3-bridge.jar"))
    //Lottie
    implementation("com.airbnb.android:lottie:6.6.0")
    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    //MMKV
    implementation("com.tencent:mmkv:1.2.13")
    //腾讯云HTTPDNS
    implementation("io.github.dnspod:httpdns-sdk:4.9.1")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // EventBus
    implementation("org.greenrobot:eventbus:3.3.1")
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    //TimetableView
    implementation("com.github.zfman:TimetableView:2.0.7")
    //Room
    implementation("androidx.room:room-runtime:2.5.2")
    kapt("androidx.room:room-compiler:2.5.2")
    // Kotlin 扩展
    implementation(files("libs/develocity-gradle-plugin-3.17.6.jar"))
    implementation("androidx.room:room-ktx:2.5.2")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.crashlytics.buildtools)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    correctErrorTypes = true
}
