// Top-level build file
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}