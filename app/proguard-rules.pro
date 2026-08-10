# Add project specific ProGuard rules here.
# Keep line numbers for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Keep Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Room entities
-keep class com.blackbox.data.** { *; }
