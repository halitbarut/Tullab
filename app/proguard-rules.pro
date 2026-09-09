# ====================================================================
# ProGuard / R8 Rules for Tullab
# ====================================================================

# --------------------------------------------------------------------
# 1. Stack Traces & Debugging (Firebase Crashlytics)
# --------------------------------------------------------------------
# Preserve line numbers and source file names for de-obfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# --------------------------------------------------------------------
# 2. Room Database
# --------------------------------------------------------------------
# Keep Room base classes and interfaces
-keep class androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep entities, fields, and constructors
-keep @androidx.room.Entity class * {
    <fields>;
    <init>(...);
}
-keep class com.barutdev.tullab.data.local.entity.** { *; }

# Keep DAO interfaces and methods
-keep @androidx.room.Dao interface * {
    <methods>;
}
-keep class com.barutdev.tullab.data.local.**Dao* { *; }

# Keep Type Converters
-keep class * extends androidx.room.TypeConverter { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-keep class com.barutdev.tullab.data.local.**Converter* { *; }
-keep class com.barutdev.tullab.data.local.TullabDatabase* { *; }

# --------------------------------------------------------------------
# 3. Hilt & Dagger Dependency Injection
# --------------------------------------------------------------------
# Keep Application, EntryPoints, and ViewModels
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep @dagger.Module class * { *; }

# Keep injected fields, constructors, and methods
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <methods>;
}

# Keep Hilt generated artifacts and factories
-keep class **_MembersInjector { *; }
-keep class **_Factory { *; }
-keep class **_HiltModules* { *; }
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# --------------------------------------------------------------------
# 4. Kotlin Coroutines
# --------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.flow.**

# --------------------------------------------------------------------
# 5. Firebase Crashlytics & Analytics
# --------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
# Explicit measurement keep rules so R8 does not strip analytics receivers
# or transport services in release builds (required for real-time reporting).
-keep class com.google.android.gms.measurement.** { *; }
-keep class com.google.firebase.analytics.** { *; }

# --------------------------------------------------------------------
# 6. Domain Models (Data State & Backup Serialization)
# --------------------------------------------------------------------
-keep class com.barutdev.tullab.domain.model.** { *; }