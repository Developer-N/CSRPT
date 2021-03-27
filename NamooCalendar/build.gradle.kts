import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-android")
}
fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String? = try {
    ProcessBuilder("\\s".toRegex().split(this))
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start().apply { waitFor(timeoutAmount, timeoutUnit) }
        .inputStream.bufferedReader().readText()
} catch (e: java.io.IOException) {
    e.printStackTrace()
    null
}

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.3")

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "ir.namoo.religiousprayers"
        minSdkVersion(17)
        targetSdkVersion(30)
        versionCode = 9200
        versionName = "9.2.2021"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true

        useLibrary("org.apache.http.legacy")
        resConfigs("en", "fa", "ckb")
    }


    buildTypes {

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation("com.github.persian-calendar:equinox:1.0.0")
    implementation("com.github.persian-calendar:calendar:1.0.1")

    implementation("androidx.multidex:multidex:2.0.1")

    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("com.github.google:flexbox-layout:2.0.1")

    implementation("com.google.android.apps.dashclock:dashclock-api:2.0.0")
    implementation("com.google.openlocationcode:openlocationcode:1.0.4")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.3.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.4")

    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.fragment:fragment-ktx:1.3.1")
    implementation("androidx.activity:activity-ktx:1.2.1")

    implementation("androidx.browser:browser:1.3.0")

    implementation("androidx.work:work-runtime-ktx:2.5.0")

    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.0-alpha-2")
    // debugImplementation("com.github.pedrovgs:lynx:1.1.0")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test:rules:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

    implementation("com.googlecode.json-simple:json-simple:1.1")

    implementation("androidx.room:room-runtime:2.2.6")
    kapt("androidx.room:room-compiler:2.2.6")
    kapt("android.arch.persistence.room:compiler:1.1.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
}