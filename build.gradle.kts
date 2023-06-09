plugins {
    id("com.android.application") version "8.0.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.21" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.5.1" apply false
    id("com.google.devtools.ksp") version "1.8.21-1.0.11" apply false

    //FireBase
    id("com.google.gms.google-services") version "4.3.15" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
    id("com.google.firebase.crashlytics") version "2.9.2" apply false
}

task("clean") {
    delete(rootProject.buildDir)
}
