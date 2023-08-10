plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("auto-register")
    id("com.google.devtools.ksp")
}

android {
    namespace = "me.wcy.serviceloader.app"
    compileSdk = 33

    defaultConfig {
        applicationId = "me.wcy.serviceloader.app"
        minSdk = 21
        targetSdk = 33
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

ksp {
    arg("moduleName", project.name)
}

autoregister {
    registerInfo = listOf(
        mapOf(
            "scanInterface" to "me.wcy.serviceloader.annotation.IServiceLoader",
            "codeInsertToClassName" to "me.wcy.serviceloader.api.ServiceLoader",
            "codeInsertToMethodName" to "init",
            "registerMethodName" to "register",
            "include" to listOf("me/wcy/serviceloader/apt/.*")
        )
    )
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    ksp(project(":service-loader-compiler"))
    implementation(project(":service-loader-api"))
    implementation(project(":apple"))
    implementation(project(":banana"))
}