import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.baselineprofile)
    id("kotlin-parcelize")
}
configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        FileInputStream(file).use { load(it) }
    }
}

val privateProperties = Properties().apply {
    val file = project.file("private.properties")
    if (file.exists()) {
        file.reader(Charsets.UTF_8).use { load(it) }
    }
}

val planetApiBaseUrlDebug: String = privateProperties.getProperty("planet.apiBaseUrl.debug")?.trim().orEmpty()
val planetApiBaseUrlRelease: String = privateProperties.getProperty("planet.apiBaseUrl.release")?.trim().orEmpty()
val buglyAppId: String = privateProperties.getProperty("bugly.appId")?.trim().orEmpty()
val planetApplicationId = providers.gradleProperty("planetApplicationId")
    .orElse("com.zhelearn.CSUSTPlanet")
    .get()
val legacyMigrationProviderEnabled = planetApplicationId == "com.example.changli_planet_app"
val legacyMigrationProviderAuthority = if (legacyMigrationProviderEnabled) {
    "com.example.changli_planet_app.migration"
} else {
    "$planetApplicationId.inactive-migration"
}

android {
    namespace = "com.zhelearn.CSUSTPlanet"
    compileSdk = 36
    compileSdkMinor = 1
    buildFeatures {
        buildConfig = true
        compose = true
    }
    
    signingConfigs {
        create("release") {
            val keyStorePath = localProperties.getProperty("release.storeFile")
                ?: System.getenv("RELEASE_STORE_FILE")
                ?: "release-key.jks"

            storeFile = file(keyStorePath)

            storePassword = localProperties.getProperty("release.storePassword")
                ?: System.getenv("RELEASE_STORE_PASSWORD")

            keyAlias = localProperties.getProperty("release.keyAlias")
                ?: System.getenv("RELEASE_KEY_ALIAS")

            keyPassword = localProperties.getProperty("release.keyPassword")
                ?: System.getenv("RELEASE_KEY_PASSWORD")
        }
    }
    
    defaultConfig {
        applicationId = planetApplicationId
        minSdk = 24
        targetSdk = 36
        versionCode = 35
        versionName = "2.11.0"
        val amapApiKey: String = privateProperties.getProperty("amap.apiKey")?.trim().orEmpty()
        manifestPlaceholders["amapApiKey"] = amapApiKey
        manifestPlaceholders["legacyMigrationProviderEnabled"] = legacyMigrationProviderEnabled.toString()
        manifestPlaceholders["legacyMigrationProviderAuthority"] = legacyMigrationProviderAuthority

        ndk {
            // 设置支持的SO库架构
            abiFilters.add("arm64-v8a")
        }

        // 兜底值：保证任何未显式覆盖的 variant 都拿得到 BaseUrl。
        // debug / release / benchmark 会在各自 buildType 内再覆盖为对应环境值。
        buildConfigField(
            "String",
            "PLANET_API_BASE_URL",
            "\"$planetApiBaseUrlRelease\""
        )
        buildConfigField(
            "String",
            "BUGLY_APP_ID",
            "\"$buglyAppId\""
        )
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "PLANET_API_BASE_URL",
                "\"$planetApiBaseUrlDebug\""
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            buildConfigField(
                "String",
                "PLANET_API_BASE_URL",
                "\"$planetApiBaseUrlRelease\""
            )
        }

        create("benchmark") {
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // benchmark 贴近线上环境，走 release 域名
            buildConfigField(
                "String",
                "PLANET_API_BASE_URL",
                "\"$planetApiBaseUrlRelease\""
            )
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

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val versionName = output.versionName.orNull ?: ""
            (output as? com.android.build.api.variant.impl.VariantOutputImpl)
                ?.outputFileName?.set("clPlanetApp_${variant.name}_v${versionName}.apk")
        }
    }
}
dependencies {
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.compose.ui.graphics)
    "baselineProfile"(project(":baselineprofile"))
    // leakcanary
    debugImplementation(libs.leakcanary.android)
    // Material Design
    //RxJava
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.androidx.room.rxjava3)
    //Glide
    implementation(libs.glide)
    ksp(libs.glide.ksp)
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
    // Gson
    implementation(libs.gson)
    //TimetableView
    implementation(libs.timetableview)
    //Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
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
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    testImplementation("junit:junit:4.13.2")
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
//    implementation(files("libs/csust_spider-release.aar"))
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.haze)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    // 高德地图 Android 轻量版 SDK（本地 jar，含地图 + 定位）
    implementation(files("libs/Lite3DMap.jar"))
}
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
