@file:Suppress("UnstableApiUsage")

import monzo.VersioningPlugin

plugins {
    id("com.android.application")
}

apply<VersioningPlugin>()

android {
    namespace = "com.monzo.releases"
    compileSdk = 33

    defaultConfig {
        minSdk = 23
        targetSdk = 33

        applicationId = "com.monzo.releases"
    }

    lint {
        // This prevents lintVital running every time we build a release variant.
        // It does *not* prevent us explicitly running lint on a release variant.
        checkReleaseBuilds = false
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("$rootDir/debug.jks")
            storePassword = "abcdef"
            keyAlias = "monzo_debug"
            keyPassword = "abcdef"
        }

        create("release") {
            // Our release process builds unsigned release APKs (using the 'unsigned' Gradle property) and
            // performs signing on the backend.
            // These properties exist so that we can build a signed release APK locally in an emergency.
            val keystore = findProperty("keystore")?.toString() ?: "default"
            val alias = findProperty("alias")?.toString() ?: "default"
            val password = findProperty("password")?.toString() ?: "default"

            storeFile = rootProject.file(keystore)
            storePassword = password
            keyAlias = alias
            keyPassword = password
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")

            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }

        release {
            signingConfig = if (project.hasProperty("unsigned")) null else signingConfigs.getByName("release")

            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
