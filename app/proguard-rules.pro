# Keep Compose generated classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Hilt
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
