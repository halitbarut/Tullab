import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(this::load)
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.barutdev.tullab"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.barutdev.tullab"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.hilt.android)
    implementation(libs.androidx.material3)
    ksp(libs.hilt.compiler)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.room.testing)
    kspAndroidTest(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}

// Automated release packaging and artifact export.
// Usage: ./gradlew :app:exportRelease
tasks.register("exportRelease") {
    dependsOn("assembleRelease", "bundleRelease")
    description = "Assembles release APK + AAB and exports them to a versioned App Releases directory."
    group = "release"

    doLast {
        val versionName = android.defaultConfig.versionName
            ?: throw GradleException("versionName is not set in android.defaultConfig")
        val versionCode = android.defaultConfig.versionCode
            ?: throw GradleException("versionCode is not set in android.defaultConfig")

        val userHome = System.getProperty("user.home")
            ?: System.getenv("HOME")
            ?: throw GradleException("Unable to resolve user home directory for release export")

        val destDir = File(userHome, "AndroidStudioProjects/App Releases/Tullab/v$versionName ($versionCode)")
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw GradleException("Failed to create release export directory: ${destDir.absolutePath}")
        }

        val buildDir = layout.buildDirectory.get().asFile
        val exportedPaths = mutableListOf<String>()

        fun exportFile(source: File, targetName: String, required: Boolean) {
            if (!source.exists()) {
                val message = "Release artifact not found, skipping: ${source.absolutePath}"
                if (required) {
                    throw GradleException(message)
                } else {
                    logger.warn("exportRelease: $message")
                }
                return
            }
            val target = File(destDir, targetName)
            if (target.exists()) {
                target.delete()
            }
            source.copyTo(target, overwrite = true)
            exportedPaths.add(target.absolutePath)
            logger.lifecycle("exportRelease: exported ${source.name} -> ${target.absolutePath}")
        }

        // Release APK and AAB, renamed with the current version.
        exportFile(
            File(buildDir, "outputs/apk/release/app-release.apk"),
            "Tullab-v$versionName.apk",
            required = true
        )
        exportFile(
            File(buildDir, "outputs/bundle/release/app-release.aab"),
            "Tullab-v$versionName.aab",
            required = true
        )

        // ProGuard/R8 mapping file (only present with minification enabled).
        exportFile(
            File(buildDir, "outputs/mapping/release/mapping.txt"),
            "mapping.txt",
            required = false
        )

        // Native debug symbols bundle, when NDK symbols are generated.
        exportFile(
            File(buildDir, "outputs/native-debug-symbols/release/native-debug-symbols.zip"),
            "Tullab-v$versionName-native-debug-symbols.zip",
            required = false
        )

        // Raw merged native libs directory, when present.
        val nativeLibsDir = File(buildDir, "intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib")
        if (nativeLibsDir.exists() && nativeLibsDir.isDirectory) {
            val targetNativeDir = File(destDir, "native-symbols")
            targetNativeDir.deleteRecursively()
            nativeLibsDir.copyRecursively(targetNativeDir, overwrite = true)
            exportedPaths.add(targetNativeDir.absolutePath)
            logger.lifecycle("exportRelease: exported native symbols -> ${targetNativeDir.absolutePath}")
        } else {
            logger.warn("exportRelease: native symbols directory not found, skipping: ${nativeLibsDir.absolutePath}")
        }

        logger.lifecycle("exportRelease: versionName=$versionName versionCode=$versionCode")
        logger.lifecycle("exportRelease: destination directory: ${destDir.absolutePath}")
        logger.lifecycle("exportRelease: completed successfully. Exported artifacts:")
        exportedPaths.forEach { path -> logger.lifecycle("exportRelease:  - $path") }
    }
}


