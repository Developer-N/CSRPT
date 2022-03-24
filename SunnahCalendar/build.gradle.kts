import org.codehaus.groovy.runtime.ProcessGroovyMethods

operator fun File.div(child: String) = File(this, child)
fun String.execute() = ProcessGroovyMethods.execute(this)
val Process.text: String? get() = ProcessGroovyMethods.getText(this)

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    kotlin("plugin.serialization") version "1.6.10"
    id("io.github.persiancalendar.appbuildplugin") apply true
}

// https://developer.android.com/jetpack/androidx/releases/compose-kotlin
val composeVersion = "1.1.1"
val composeSecondaryVersion = "1.1.1"

val isMinApi21Build = gradle.startParameter.taskNames.any { "minApi21" in it || "MinApi21" in it }

val generatedAppSrcDir = buildDir / "generated" / "source" / "appsrc" / "main"
android {
    sourceSets {
        getByName("main").kotlin.srcDir(generatedAppSrcDir)
    }

    compileSdk = 31
    buildToolsVersion = "30.0.3"

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "ir.namoo.religiousprayers"
        minSdk = 19 // if (enableFirebaseInNightlyBuilds) 19 else 17
        targetSdk = 31
        versionCode = 10000
        versionName = "10.0.2022"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (!isMinApi21Build) vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
        resourceConfigurations += listOf(
            "en", "fa", "ckb", "ar", "ur", "ps", "glk", "azb", "ja", "fr", "es", "tr", "kmr", "tg",
            "ne", "zh-rCN"
        )
        setProperty("archivesBaseName", "SunnahCalendar-$versionName")
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    buildTypes {

        getByName("debug") {
            buildConfigField("boolean", "DEVELOPMENT", "true")
            multiDexEnabled = true
        }

        getByName("release") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isMinifyEnabled = true
            isShrinkResources = true
            multiDexEnabled = true
            buildConfigField("boolean", "DEVELOPMENT", "false")
        }
    }
    flavorDimensions += listOf("api")

    productFlavors {
        create("minApi19") {
            dimension = "api"
        }
        create("minApi21") {
            applicationIdSuffix = ".minApi21"
            dimension = "api"
            minSdk = 21
            // versionCode = versionNumber + 1
        }
    }

    packagingOptions {
        resources.excludes += "DebugProbesKt.bin"
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    bundle {
        language {
            // We have in app locale change and don't want Google Play's dependency so better
            // to disable this.
            enableSplit = false
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }

    if (isMinApi21Build) {
        buildFeatures {
            compose = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        // isCoreLibraryDesugaringEnabled = true
        //   Actually could be useful as makes use of java.time.Duration possible instead
        //   java.util.concurrent.TimeUnit but needs multidex as it says:
        //     In order to use core library desugaring, please enable multidex.
        //   And multidex doesn't play that well for older Android versions so let's
        //   skip it.
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    lint { disable += listOf("MissingTranslation") }

    applicationVariants.all {
        val variant = this
        variant.outputs.map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val outputFileName =
                    "SRP-v${variant.versionName}-${variant.flavorName}-${variant.buildType.name}.apk"
                println("OutputFileName: $outputFileName")
                output.outputFileName = outputFileName
            }
    }
}

val minApi21Implementation by configurations

dependencies {
    implementation("com.github.persian-calendar:equinox:2.0.0")
    implementation("com.github.persian-calendar:calendar:1.2.0")
    implementation("com.github.persian-calendar:praytimes:2.1.2")
    implementation("com.github.persian-calendar:calculator:9a8b4980873f8acf83cf119cf9bf3e31e5259c1d")

    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("com.google.android.material:material:1.5.0")

    val navVersion = "2.4.1"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    androidTestImplementation("androidx.navigation:navigation-testing:$navVersion")

    implementation("androidx.core:core-ktx:1.7.0")
    val fragmentVersion = "1.4.1"
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    debugImplementation("androidx.fragment:fragment-testing:$fragmentVersion")
    implementation("androidx.activity:activity-ktx:1.4.0")

    implementation("androidx.browser:browser:1.4.0")

    implementation("androidx.work:work-runtime-ktx:2.7.1")

    val coroutinesVersion = "1.6.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")

    implementation("com.google.openlocationcode:openlocationcode:1.0.4")
    implementation("com.google.zxing:core:3.4.1")

    // Only needed for debug builds for now, won't be needed for minApi21 builds either
    debugImplementation("com.android.support:multidex:2.0.0")

    minApi21Implementation("androidx.activity:activity-compose:1.4.0")
    minApi21Implementation("com.google.android.material:compose-theme-adapter:1.1.5")
    val accompanistVersion = "0.23.1"
    minApi21Implementation("com.google.accompanist:accompanist-flowlayout:$accompanistVersion")
    minApi21Implementation("com.google.accompanist:accompanist-drawablepainter:$accompanistVersion")
    minApi21Implementation("androidx.compose.ui:ui:$composeVersion")
    minApi21Implementation("androidx.compose.material:material:$composeSecondaryVersion")
    minApi21Implementation("androidx.compose.material3:material3:1.0.0-alpha07")
    minApi21Implementation("androidx.compose.ui:ui-tooling-preview:$composeSecondaryVersion")
    if (isMinApi21Build) {
        implementation("androidx.compose.runtime:runtime:$composeVersion")
        androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeSecondaryVersion")
    }
    minApi21Implementation("androidx.compose.ui:ui-tooling:$composeSecondaryVersion")

    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.8.1")

    testImplementation("junit:junit:4.13.2")

    testImplementation("org.junit.platform:junit-platform-runner:1.8.2")
    val junit5Version = "5.8.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit5Version")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

    testImplementation("com.google.truth:truth:1.1.3")
    // Scratch.kt only dependencies
    // testImplementation("com.squareup.okhttp3:okhttp:3.10.0")
    // testImplementation("org.json:json:20210307")
    // testImplementation("com.ibm.icu:icu4j:68.2")

    val androidTestVersion = "1.4.0"
    androidTestImplementation("androidx.test:runner:$androidTestVersion")
    androidTestImplementation("androidx.test:rules:$androidTestVersion")
    androidTestImplementation("androidx.test:core-ktx:$androidTestVersion")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    val espressoVersion = "3.4.0"
    androidTestImplementation("androidx.test.espresso:espresso-contrib:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.41")
    kapt("com.google.dagger:hilt-android-compiler:2.41")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    //Ktor
    implementation("io.ktor:ktor-client-core:1.6.7")
    implementation("io.ktor:ktor-client-serialization:1.6.7")
    implementation("io.ktor:ktor-client-logging:1.6.7")
    minApi21Implementation("io.ktor:ktor-client-okhttp:1.6.7")
    implementation("io.ktor:ktor-client-android:1.6.7")
    implementation("ch.qos.logback:logback-classic:1.2.6")


    //Json Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    //room
    implementation("androidx.room:room-runtime:2.4.2")
    kapt("androidx.room:room-compiler:2.4.2")
    implementation("androidx.room:room-ktx:2.4.2")
    kapt("android.arch.persistence.room:compiler:1.1.1")

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")

    implementation("androidx.multidex:multidex:2.0.1")

    //Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    //OkHttp
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.6")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.6")

    //Reflection
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")

    //Zip jar file
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))

    implementation("com.android.support:multidex:2.0.0")
    implementation("com.github.google:flexbox-layout:2.0.1")

}

tasks.named("preBuild").configure { dependsOn(getTasksByName("codegenerators", false)) }

tasks.register("moveToApiFlavors") {
    doLast {
        val source = gradle.startParameter.projectProperties["fileName"]
            ?: error("Moves a source file to api flavors\nPass -P fileName=FILENAME to this")
        if ("/main/" !in source) error("File name should be a source file in the main flavor")
        if (!File(source).isFile) error("Source file name doesn't exist")
        val minApi19Target = source.replace("/main/", "/minApi19/")
        File(File(minApi19Target).parent).mkdirs()
        val minApi21Target = source.replace("/main/", "/minApi21/")
        File(File(minApi21Target).parent).mkdirs()
        println("cp $source $minApi21Target".execute().text)
        println("git add $minApi21Target".execute().text)
        println("git mv $source $minApi19Target".execute().text)
        println("git status".execute().text)
    }
}

tasks.register("mergeWeblate") {
    doLast {
        val weblateRepository = "https://hosted.weblate.org/git/persian-calendar/persian-calendar/"
        println("git remote add weblate $weblateRepository".execute().text)
        println("git remote update weblate".execute().text)
        println("git merge weblate/main".execute().text)
    }
}
