# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/jordifierro/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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

# Okhttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Retrofit
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain service method parameters.
-keepclassmembernames,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
# Mappers
-keep class com.pachatary.data.auth.AuthTokenMapper { *; }
-keep class com.pachatary.data.auth.ClientExceptionMapper { *; }
-keep class com.pachatary.data.auth.ClientVersionsMapper { *; }
-keep class com.pachatary.data.auth.PersonMapper { *; }
-keep class com.pachatary.data.auth.PairPersonAuthTokenMapper { *; }
-keep class com.pachatary.data.common.PaginatedListMapper { *; }
-keep class com.pachatary.data.experience.ExperienceMapper { *; }
-keep class com.pachatary.data.picture.PictureMapper { *; }
-keep class com.pachatary.data.auth.AuthTokenMapper { *; }
-keep class com.pachatary.data.scene.SceneMapper { *; }

# Dagger
-dontwarn com.google.errorprone.annotations.**

# Picasso
-dontwarn com.squareup.okhttp.**

# Matisse
-dontwarn com.bumptech.glide.**

-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }
