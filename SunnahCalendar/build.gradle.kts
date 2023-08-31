import org.codehaus.groovy.runtime.ProcessGroovyMethods

operator fun File.div(child: String) = File(this, child)
fun String.execute() = ProcessGroovyMethods.execute(this)
val Process.text: String? get() = ProcessGroovyMethods.getText(this)

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")
    kotlin("plugin.serialization") version "1.9.0"
}

// https://developer.android.com/jetpack/androidx/releases/compose-kotlin
val composeCompilerVersion = "1.5.2"
val composeVersion = "1.5.0"

val isMinApi21Build = gradle.startParameter.taskNames.any { "minApi21" in it || "MinApi21" in it }

val generatedAppSrcDir = buildDir / "generated" / "source" / "appsrc" / "main"
android {
    sourceSets {
        getByName("main").kotlin.srcDir(generatedAppSrcDir)
    }

    compileSdk = 34
    buildToolsVersion = "33.0.2"

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }

    namespace = "com.byagowi.persiancalendar"

    defaultConfig {
        applicationId = "ir.namoo.religiousprayers"
        minSdk = 21 // if (enableFirebaseInNightlyBuilds) 19 else 17
        targetSdk = 34
        versionCode = 10811
        versionName = "10.8.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (!isMinApi21Build) vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
        resourceConfigurations += listOf(
            "en", "fa", "ckb", "ar", "ur", "ps", "glk", "azb", "ja", "fr", "es", "tr", "kmr", "tg",
            "ne", "zh-rCN", "ru"
        )
        setProperty("archivesBaseName", "SunnahCalendar-$versionName")
    }

    testOptions.unitTests.all { it.useJUnitPlatform() }

    buildTypes {

        getByName("debug") {
            buildConfigField("boolean", "DEVELOPMENT", "true")
            multiDexEnabled = true
        }

        getByName("release") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            isMinifyEnabled = true
            isShrinkResources = true
            multiDexEnabled = true
            buildConfigField("boolean", "DEVELOPMENT", "false")
        }
    }
    flavorDimensions += listOf("api")

    productFlavors {
//        create("minApi17") {
//            dimension = "api"
//        }
        create("minApi21") {
//            applicationIdSuffix = ".minApi21"
            dimension = "api"
            minSdk = 21
            // versionCode = versionNumber + 1
        }
    }

    packaging {
        resources.excludes += "DebugProbesKt.bin"
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        resources.excludes += "META-INF/INDEX.LIST"
    }

    bundle {
        // We have in app locale change and don't want Google Play's dependency so better to disable
        language.enableSplit = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
    }

    val javaVersion = JavaVersion.VERSION_17

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        // isCoreLibraryDesugaringEnabled = true
        //   Actually could be useful as makes use of java.time.Duration possible instead
        //   java.util.concurrent.TimeUnit but needs multidex as it says:
        //     In order to use core library desugaring, please enable multidex.
        //   And multidex doesn't play that well for older Android versions so let's
        //   skip it.
    }

    kotlinOptions {
        jvmTarget = javaVersion.majorVersion
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
    // Project owned libraries
    implementation("com.github.persian-calendar:calendar:aa50eff1684cfaceac752c5d812b96e1c0ff95fa")
    implementation("com.github.persian-calendar:praytimes:36eab5bf0b6f5057357d6fbf0007bea3fab73895")
    implementation("com.github.persian-calendar:calculator:371a91149d1fea9c318ef0def94ca0f93a1be0c2")
    implementation("com.github.persian-calendar:qr:60ad1863978b35205549eae6af177c45a1e67307")

    // The only runtime third part dependency created in a collaboration, https://github.com/cosinekitty/astronomy/releases/tag/v2.1.0
    // bd2db6a3805ac8a7c559b6b2276e16c1e1793d1f is equal to v2.1.17, the latest release
    implementation("com.github.cosinekitty:astronomy:bd2db6a3805ac8a7c559b6b2276e16c1e1793d1f")

    // Google/JetBrains owned libraries (roughly platform libraries)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("com.google.android.material:material:1.9.0")

    val navVersion = "2.7.0"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    androidTestImplementation("androidx.navigation:navigation-testing:$navVersion")

    implementation("androidx.core:core-ktx:1.10.1")
    val fragmentVersion = "1.6.1"
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    debugImplementation("androidx.fragment:fragment-testing:$fragmentVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    implementation("androidx.browser:browser:1.6.0")

    implementation("androidx.work:work-runtime-ktx:2.8.1")

    val coroutinesVersion = "1.7.3"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.9.1")

    implementation("com.google.openlocationcode:openlocationcode:1.0.4")

    // Only needed for debug builds for now, won't be needed for minApi21 builds either
    debugImplementation("androidx.multidex:multidex:2.0.1")

    val activityVersion = "1.7.2"
    implementation("androidx.activity:activity-ktx:$activityVersion")
    minApi21Implementation("androidx.activity:activity-compose:$activityVersion")

    val accompanistVersion = "0.32.0"
    minApi21Implementation("com.google.accompanist:accompanist-flowlayout:$accompanistVersion")
    minApi21Implementation("com.google.accompanist:accompanist-drawablepainter:$accompanistVersion")
    minApi21Implementation("com.google.accompanist:accompanist-themeadapter-material3:$accompanistVersion")
    minApi21Implementation("androidx.compose.ui:ui:$composeVersion")
    minApi21Implementation("androidx.compose.material3:material3:1.1.1")
    minApi21Implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
//    if (isMinApi21Build) {
    implementation("androidx.compose.runtime:runtime:$composeVersion")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
//    }

    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.8.1")

    testImplementation("junit:junit:4.13.2")

    testImplementation(kotlin("test"))

    testImplementation("org.junit.platform:junit-platform-runner:1.10.0")
    val junit5Version = "5.10.0"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit5Version")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")

    testImplementation("com.google.truth:truth:1.1.5")

    androidTestImplementation("androidx.test:runner:1.5.2")
    val androidTestVersion = "1.5.0"
    androidTestImplementation("androidx.test:rules:$androidTestVersion")
    androidTestImplementation("androidx.test:core-ktx:$androidTestVersion")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    val espressoVersion = "3.5.1"
    androidTestImplementation("androidx.test.espresso:espresso-contrib:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")

    // Koin main features for Android
    implementation("io.insert-koin:koin-android:3.4.2")
    // Koin Jetpack WorkManager
    implementation("io.insert-koin:koin-androidx-workmanager:3.4.2")
    // Koin Navigation Graph
    implementation("io.insert-koin:koin-androidx-navigation:3.4.2")
    // Koin Jetpack Compose
    implementation("io.insert-koin:koin-androidx-compose:3.4.5")
    // Koin Koin for Ktor
    implementation("io.insert-koin:koin-ktor:3.4.1")
    // Koin SLF4J Logger
    implementation("io.insert-koin:koin-logger-slf4j:3.4.1")

    //Ktor
    val ktorVersion = "2.2.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.4.7")

    //Json Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    //room
    implementation("androidx.room:room-runtime:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    implementation("androidx.multidex:multidex:2.0.1")

    //FireBase
    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-inappmessaging-display-ktx")

    //Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    //OkHttp
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")

    //Reflection
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")

    //Zip jar file
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))

    //Compose
    implementation("androidx.navigation:navigation-compose:2.7.0")
    implementation("androidx.compose.animation:animation-graphics:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("com.github.skydoves:landscapist-bom:2.2.3")
    implementation("com.github.skydoves:landscapist-glide")
    implementation("com.github.skydoves:landscapist-placeholder")

    //ExoPlayer
    val mediaVersion = "1.1.1"
    implementation("androidx.media3:media3-ui:$mediaVersion")
    implementation("androidx.media3:media3-exoplayer:$mediaVersion")
    implementation("androidx.media3:media3-session:$mediaVersion")

    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.accompanist:accompanist-permissions:0.31.1-alpha")
}


// Called like: ./gradlew moveToApiFlavors -PfileName=
tasks.register("moveToApiFlavors") {
    doLast {
        val source = gradle.startParameter.projectProperties["fileName"]
            ?: error("Moves a source file to api flavors\nPass -P fileName=FILENAME to this")
        if ("/main/" !in source) error("File name should be a source file in the main flavor")
        if (!File(source).isFile) error("Source file name doesn't exist")
        val minApi17Target = source.replace("/main/", "/minApi17/")
        File(File(minApi17Target).parent).mkdirs()
        val minApi21Target = source.replace("/main/", "/minApi21/")
        File(File(minApi21Target).parent).mkdirs()
        listOf(
            "cp $source $minApi21Target",
            "git add $minApi21Target",
            "git mv $source $minApi17Target",
            "git status",
        ).forEach { println(it.execute().text) }
    }
}

tasks.register("mergeWeblate") {
    doLast {
        val weblateRepository = "https://hosted.weblate.org/git/persian-calendar/persian-calendar/"
        listOf(
            "git remote add weblate $weblateRepository",
            "git remote update weblate",
            "git merge weblate/main",
        ).forEach { println(it.execute().text) }
    }
}