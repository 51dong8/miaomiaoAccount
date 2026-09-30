plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.miaomiao.jizhang"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.miaomiao.jizhang"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"
    }

    signingConfigs {
        create("release") {
            // 签名属性：优先读本地 keystore.properties（不入库），缺失时回退 gradle.properties
            storeFile = rootProject.file(providers.gradleProperty("RELEASE_STORE_FILE").get())
            val kp = rootProject.file("keystore.properties").takeIf { it.exists() }
                ?.readLines()
                ?.mapNotNull { l ->
                    l.trim().takeIf { it.isNotBlank() && !it.startsWith("#") }
                        ?.split("=", limit = 2)
                }
                ?.associate { it[0].trim() to it[1].trim() }
                ?: emptyMap()
            fun sig(name: String): String =
                kp[name] ?: providers.gradleProperty(name).get()
            storePassword = sig("RELEASE_STORE_PASSWORD")
            keyAlias = sig("RELEASE_KEY_ALIAS")
            keyPassword = sig("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
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
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.datastore.preferences)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    implementation(libs.workmanager)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    testImplementation(libs.junit)
}
