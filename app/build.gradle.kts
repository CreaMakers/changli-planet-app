import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.changli_planet_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.changli_planet_app"
        minSdk = 24
        targetSdk = 34
        versionCode = 7
        versionName = "1.1.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            // 设置支持的SO库架构
            abiFilters.add("armeabi") //, 'x86', 'armeabi-v7a', 'x86_64', 'arm64-v8a'
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86")
            abiFilters.add("armeabi-v7a")
        }
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

    applicationVariants.all {
        outputs.all {
            val buildType = buildType.name
            val versionName = versionName

            (this as BaseVariantOutputImpl).outputFileName =
                "clPlanetApp_${buildType}_v${versionName}.apk"

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
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
    buildFeatures {
        viewBinding = true
    }
}
dependencies {
    // leakcanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
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
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    //MMKV
    implementation("com.tencent:mmkv:1.2.13")
    //腾讯云HTTPDNS
    implementation("io.github.dnspod:httpdns-sdk:4.9.1")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.6.1")
    implementation("com.squareup.retrofit2:converter-gson:2.6.1")
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

    // 图片裁剪库
    implementation("com.github.Yalantis:uCrop:2.2.9")
    // PhotoView
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    // SubsamplingScaleImageView
    implementation("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")
    // PhotoView
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    // Activity KTX for viewModels()
    implementation("androidx.activity:activity-ktx:1.8.2")
    // Fragment KTX 提供了 viewModels() 扩展函数
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    //滚轮
    implementation("com.github.gzu-liyujiang.AndroidPicker:Common:4.0.1")
    implementation("com.github.gzu-liyujiang.AndroidPicker:WheelPicker:4.0.1")
    //SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    //FlexboxLayout
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    //SmartRefreshLayout
    implementation("io.github.scwang90:refresh-layout-kernel:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-header-radar:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-header-material:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-footer-classics:3.0.0-alpha")
    // BRV，尝试一下
    implementation("com.github.liangjingkanji:BRV:1.6.1")


    //lottie
    implementation("com.airbnb.android:lottie:6.0.0")
    //bugly
    implementation("com.tencent.bugly:crashreport:4.1.9.3")
    // 缺省页
    implementation("com.github.liangjingkanji:StateLayout:1.4.2")
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
