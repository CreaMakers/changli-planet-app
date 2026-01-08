# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontwarn com.google.re2j.**
-dontwarn org.jsoup.helper.Re2jRegex**

-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.stream.** { *; }
-keepclassmembers class * {
    <init>();
}
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.creamaker.changli_planet_app.**.bean.** { *; }
-keep class com.creamaker.changli_planet_app.**.model.** { *; }
-keep class com.creamaker.changli_planet_app.**.dto.** { *; }

# --- Baseline Profile Installer ---
-keep class androidx.profileinstaller.** { *; }
-keep class com.google.tools.profiler.** { *; }
-dontwarn androidx.profileinstaller.**
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type