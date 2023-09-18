import org.codehaus.groovy.runtime.ProcessGroovyMethods

operator fun File.div(child: String) = File(this, child)
fun String.execute() = ProcessGroovyMethods.execute(this)
val Process.text: String? get() = ProcessGroovyMethods.getText(this)

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.plugin.serialization)
}

val isMinApi21Build = gradle.startParameter.taskNames.any { "minApi21" in it || "MinApi21" in it }

val generatedAppSrcDir =
    layout.buildDirectory.get().asFile / "generated" / "source" / "appsrc" / "main"
android {
    sourceSets {
        getByName("main").kotlin.srcDir(generatedAppSrcDir)
    }

    compileSdk = 34

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
        versionCode = 10820
        versionName = "10.8.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (!isMinApi21Build) vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
        resourceConfigurations += listOf(
            "en",
            "fa",
            "ckb",
            "ar",
            "ur",
            "ps",
            "glk",
            "azb",
            "ja",
            "fr",
            "es",
            "tr",
            "kmr",
            "tg",
            "ne",
            "zh-rCN",
            "ru"
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
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
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
                    if (variant.buildType.name == "debug")
                        "SRP-v${variant.versionName}-${variant.buildType.name}-${variant.versionCode}.apk"
                    else "SRP-v${variant.versionName}-NamooIR.apk"
                println("OutputFileName -> $outputFileName")
                output.outputFileName = outputFileName
            }
    }
}

val minApi21Implementation by configurations

dependencies {
    // Project owned libraries
    implementation(libs.persiancalendar.calendar)
    implementation(libs.persiancalendar.praytimes)
    implementation(libs.persiancalendar.calculator)
    implementation(libs.persiancalendar.qr)

    // The only runtime third part dependency created in a collaboration, https://github.com/cosinekitty/astronomy/releases/tag/v2.1.0
    // bd2db6a3805ac8a7c559b6b2276e16c1e1793d1f is equal to v2.1.17, the latest release
    implementation(libs.astronomy)

    // Google/JetBrains owned libraries (roughly platform libraries)
    implementation(libs.appcompat)
    implementation(libs.preference.ktx)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.viewpager2)
    implementation(libs.dynamicanimation)
    implementation(libs.material)

    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    androidTestImplementation(libs.navigation.testing)

    implementation(libs.androidx.core.ktx)
    implementation(libs.fragment.ktx)
    debugImplementation(libs.fragment.testing)
    implementation(libs.lifecycle.runtime.ktx)

    implementation(libs.browser)

    implementation(libs.work.manager.ktx)

    implementation(libs.kotlinx.coroutines.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    implementation(libs.kotlinx.html.jvm)

    implementation(libs.openlocationcode)

    // Not used directly on the app but is used by work manager anyway
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // Only needed for debug builds for now, won't be needed for minApi21 builds either
    debugImplementation(libs.multidex)

    implementation(libs.activity.ktx)
    minApi21Implementation(libs.compose.activity)

    minApi21Implementation(libs.bundles.compose.accompanist)
    minApi21Implementation(libs.compose.ui)
    minApi21Implementation(libs.compose.material3)
    minApi21Implementation(libs.compose.ui.tooling.preview)
    if (isMinApi21Build) {
        implementation(libs.compose.runtime)
        androidTestImplementation(libs.compose.ui.test.junit4)
        debugImplementation(libs.compose.ui.tooling)
    }

    // debugImplementation(libs.leakcanary)

    testImplementation(libs.junit)

    testImplementation(kotlin("test"))

    testImplementation(libs.junit.platform.runner)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(libs.bundles.mockito)

    testImplementation(libs.truth)

    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.core.ktx)
    androidTestImplementation(libs.androidx.test.ext.junit)

    androidTestImplementation(libs.bundles.espresso)

    // Koin main features for Android
    implementation(libs.koin.android)
    // Koin Jetpack WorkManager
    implementation(libs.koin.workmanager)
    // Koin Navigation Graph
    implementation(libs.koin.navigation)
    // Koin Jetpack Compose
    implementation(libs.koin.compose)
    // Koin for Ktor
    implementation(libs.koin.ktor)
    // Koin SLF4J Logger
    implementation(libs.koin.logger)

    //Ktor
    implementation(libs.bundles.ktor)
    implementation(libs.ch.gos.logback)

    //Json Serialization
    implementation(libs.kotlinx.serialization.json)

    //room
    ksp(libs.room.compiler)

    implementation(libs.lifecycle.extensions)
    implementation(libs.lifecycle.livedata)

    implementation(libs.multidex)

    //FireBase
    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-inappmessaging-display-ktx")

    //Timber
    implementation(libs.timber)

    //OkHttp
    implementation(libs.okhttp3.okhttp)
    implementation(libs.okhttp3.logging)

    //Reflection
    implementation(libs.kotlin.reflect)

    //Zip jar file
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))

    //Compose
    implementation(libs.navigation.compose)
    implementation(libs.compose.animation.graphics)
    implementation(libs.compose.extended.icons)
    implementation(libs.skydoves.bom)
    implementation("com.github.skydoves:landscapist-glide")
    implementation("com.github.skydoves:landscapist-placeholder")

    //ExoPlayer
    implementation(libs.bundles.media3)

    implementation(libs.gms.location)
    implementation(libs.appcomanist.permissions)
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