pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://maven.aliyun.com/repository/google") // 阿里云 Google 仓库镜像
        maven(url = "https://maven.aliyun.com/repository/central") // 阿里云中央仓库镜像
//        maven { url = uri("https://jitpack.io") }
        maven(url = "https://jitpack.io") // JitPack
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // 强制在这里统一配置仓库
    repositories {
        google() // 官方 Google 仓库
        mavenCentral() // 官方 Maven 中央仓库
        flatDir {
            dirs("app/libs") // 指定本地文件夹
        }
        maven { url = uri("https://jitpack.io") }
        maven(url = "https://maven.aliyun.com/repository/google") // 阿里云 Google 仓库镜像
        maven(url = "https://maven.aliyun.com/repository/central") // 阿里云中央仓库镜像
        maven(url = "https://jitpack.io") // JitPack 仓库
    }
}

rootProject.name = "changli-planet-app"
include(":app")
include(":CP_Common")
include(":baselineprofile")
