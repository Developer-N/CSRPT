plugins {
    id("com.android.application") version "7.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.7.20" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.5.1" apply false

    //FireBase
    id("com.google.gms.google-services") version "4.3.10" apply false
    id("com.google.firebase.firebase-perf") version "1.4.1" apply false
    id("com.google.firebase.crashlytics") version "2.8.1" apply false
}

task("clean") {
    delete(rootProject.buildDir)
}
