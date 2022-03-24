plugins {
    id("com.android.application") version "7.1.2" apply false
    id("com.android.library") version "7.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.6.10" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.4.1" apply false

    //Hilt DI
    id("dagger.hilt.android.plugin") version "2.41" apply false
}

task("clean") {
    delete(rootProject.buildDir)
}
