// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.0.0" apply false
    id("com.android.library") version "7.0.0" apply false
    id("org.jetbrains.kotlin.android") version "1.7.10" apply false
    id("org.jetbrains.kotlin.jvm") version "1.7.10" apply false
}

buildscript {
    dependencies {
        classpath("com.billy.android:autoregister:1.4.2")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}