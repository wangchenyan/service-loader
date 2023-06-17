plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("auto-register")
    id("kotlin-kapt")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "me.wcy.serviceloader.app"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

kapt {
    arguments {
        arg("moduleName", project.name)
    }
}

autoregister {
    registerInfo = ArrayList<Map<String, Any>?>().apply {
        add(
            mapOf(
                "scanInterface" to "me.wcy.serviceloader.annotation.IServiceLoader",
                "codeInsertToClassName" to "me.wcy.serviceloader.api.ServiceLoader",
                "codeInsertToMethodName" to "init",
                "registerMethodName" to "register",
                "include" to listOf("me/wcy/serviceloader/apt/.*")
            )
        )
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    kapt(project(":service-loader-compiler"))
    implementation(project(":service-loader-api"))
    implementation(project(":apple"))
    implementation(project(":banana"))
}