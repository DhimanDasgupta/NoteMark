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
# Preserve line numbers for readable release crash stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# WorkManager creates workers reflectively from the stored worker class name.
# Keep the worker class and its required constructor.
-keep class com.dhimandasgupta.notemark.app.work.NoteSyncWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep Kotlin metadata. Useful for libraries that inspect Kotlin declarations.
-keep class kotlin.Metadata { *; }

# Keep kotlinx.serialization generated serializers used by reflective lookup/fallback paths.
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class **$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep app network DTO names/members stable for JSON serialization safety.
# If all DTOs are @Serializable and referenced directly, R8 can often shrink safely,
# but this avoids release-only serialization surprises.
-keep class com.dhimandasgupta.notemark.data.remote.model.** { *; }

# Keep protobuf generated message classes used by DataStore/protobuf-lite.
-keep class com.dhimandasgupta.notemark.proto.** { *; }

# Ktor/OkHttp/Android networking stacks can reference optional platform classes.
-dontwarn org.slf4j.**
-dontwarn javax.naming.**
-dontwarn javax.annotation.**