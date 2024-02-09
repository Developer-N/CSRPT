plugins {
    // All the plugins used in subprojects and plugins should be listed here with "apply false"

    // PersianCalendar plugins
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // gradlePlugins plugins
    `kotlin-dsl` apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false

    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.parcelize) apply false

    //FireBase
    alias(libs.plugins.google.gms) apply false
    alias(libs.plugins.firebase.pref) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

task("clean") {
    delete(rootProject.layout.buildDirectory)
}
