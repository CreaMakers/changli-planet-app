import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("kotlin-parcelize")
}
configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
}
android {
    namespace = "com.creamaker.changli_planet_app"
    compileSdk = 36
    buildFeatures {
        buildConfig = true
        compose = true
    }
    
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("RELEASE_STORE_FILE") ?: "release-key.jks")
            storePassword = System.getenv("RELEASE_STORE_PASSWORD")
            keyAlias = System.getenv("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
        }
    }
    
    defaultConfig {
        applicationId = "com.example.changli_planet_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 17
        versionName = "1.3.8.1"


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
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
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
    debugImplementation(libs.leakcanary.android)
    // Material Design
    //RxJava
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.androidx.room.rxjava3)
    //Glide
    implementation(libs.glide)
    kapt(libs.glide.compiler)
    //MMKV
    implementation(libs.mmkv)
    //腾讯云HTTPDNS
    implementation(libs.httpdns.sdk)
    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp.urlconnection)
    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    // EventBus
    implementation(libs.eventbus)
    // Gson
    implementation(libs.gson)
    //TimetableView
    implementation(libs.timetableview)
    //Room
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    // 图片裁剪库
    implementation(libs.ucrop)
    // PhotoView

    // SubsamplingScaleImageView
    implementation(libs.subsampling.scale.image.view)
    // PhotoView
    implementation(libs.photoview)
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // Activity KTX for viewModels()
    implementation(libs.androidx.activity.ktx)
    // Fragment KTX 提供了 viewModels() 扩展函数
    implementation(libs.androidx.fragment.ktx)
    //滚轮
    implementation(libs.common)
    implementation(libs.wheelpicker)
    //FlexboxLayout
    implementation(libs.flexbox)
    //lottie
    implementation(libs.lottie)
    //bugly
    implementation(libs.crashreport)
    // 缺省页
    implementation(libs.statelayout)
    // jsoup
    implementation(libs.jsoup)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // workmanager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.work.rxjava2)
    implementation(project(":CP_Common"))
    //CodeLocator
    debugImplementation(libs.codelocator.core)
    debugImplementation(libs.codelocator.lancet.all)
    // Coil for image loading
    implementation(libs.coil.compose)
    //csustDataGet
    implementation(libs.csustdataget)
    implementation(libs.androidx.constraintlayout.compose)
}
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    correctErrorTypes = true
}
