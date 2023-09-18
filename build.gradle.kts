plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.androidx.navigation.safeargs.kotlin) apply false
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
