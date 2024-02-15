import org.codehaus.groovy.runtime.ProcessGroovyMethods

operator fun File.div(child: String): File = File(this, child)
fun String.execute(): Process = ProcessGroovyMethods.execute(this)
val Process.text: String? get() = ProcessGroovyMethods.getText(this)

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.plugin.serialization)
}

val generatedAppSrcDir =
    layout.buildDirectory.get().asFile / "generated" / "source" / "appsrc" / "main"
android {
    sourceSets {
        getByName("main").kotlin.srcDir(generatedAppSrcDir)
    }

    compileSdk = 34

    buildFeatures {
        buildConfig = true
        compose = true
    }

    namespace = "com.byagowi.persiancalendar"

    defaultConfig {
        applicationId = "ir.namoo.religiousprayers"
        minSdk = 21
        targetSdk = 34
        versionCode = 11022
        versionName = "11.0.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // It lowers the APK size and prevents crash in AboutScreen in API 21-23
        vectorDrawables.useSupportLibrary = true
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
    flavorDimensions += listOf("markets")
    productFlavors {
        create("namooIR") {
            dimension = "markets"
        }
        create("cafebazar") {
            dimension = "markets"
        }
        create("myket") {
            dimension = "markets"
            val marketApplicationId = "ir.mservices.market"
            val marketBindAddress = "ir.mservices.market.InAppBillingService.BIND"
            manifestPlaceholders["marketApplicationId"] = marketApplicationId
            manifestPlaceholders["marketBindAddress"] = marketBindAddress
            manifestPlaceholders["marketPermission"] = "${marketApplicationId}.BILLING"
            buildConfigField(
                "String",
                "IAB_PUBLIC_KEY",
                "your-key"
            )

        }
    }

    packaging {
        resources.excludes += "DebugProbesKt.bin"
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        resources.excludes += "/META-INF/INDEX.LIST"
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
                    if (variant.buildType.name == "debug") "SRP-v${variant.versionName}-${variant.buildType.name}-${variant.flavorName}-${variant.versionCode}.apk"
                    else "SRP-v${variant.versionName}-${variant.flavorName}.apk"
                println("OutputFileName -> $outputFileName")
                output.outputFileName = outputFileName
            }
    }
}
val cafebazarImplementation by configurations
val myketImplementation by configurations

dependencies {
    // Project owned libraries
    implementation(libs.persiancalendar.calendar)
    implementation(libs.persiancalendar.praytimes)
    implementation(libs.persiancalendar.calculator)
    implementation(libs.persiancalendar.qr)

    // https://github.com/cosinekitty/astronomy/releases/tag/v2.1.0
    implementation(libs.astronomy)

    // Google/JetBrains owned libraries (roughly platform libraries)
    implementation(libs.dynamicanimation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.browser)
    implementation(libs.work.manager.ktx)
    implementation(libs.kotlinx.coroutines.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    implementation(libs.kotlinx.html.jvm)
    implementation(libs.openlocationcode)
    implementation(libs.activity.ktx)

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.compose.activity)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.compose.animation)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.runtime)
    implementation(libs.compose.material.icons.extended)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)

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

    implementation(libs.bundles.room)
    annotationProcessor(libs.room.compiler)

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
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
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
    implementation(libs.skydoves.glide)
    implementation(libs.skydoves.placeholder)

    //ExoPlayer
    implementation(libs.bundles.media3)

    implementation(libs.gms.location)
    implementation(libs.appcomanist.permissions)

    cafebazarImplementation(libs.cafebazar.poolakey)
    myketImplementation(libs.myket.billing.client)
}


//// Just a personal debugging tool, isn't that useful as it doesn't resolve all the dependencies
//// later to be completed by ideas of
//// https://github.com/ProtonMail/proton-mail-android/blob/release/scripts/extract_dependencies/ExtractDeps.kts
//val dependenciesURLs: Sequence<Pair<String, URL?>>
//    get() = project.configurations.getByName(
//        "implementation"
//    ).dependencies.asSequence().mapNotNull {
//        it.run { "$group:$name:$version" } to project.repositories.mapNotNull { repo ->
//            (repo as? UrlArtifactRepository)?.url
//        }.flatMap { repoUrl ->
//            "%s/%s/%s/%s/%s-%s".format(
//                repoUrl.toString().trimEnd('/'),
//                it.group?.replace('.', '/') ?: "", it.name, it.version,
//                it.name, it.version
//            ).let { x -> listOf("$x.jar", "$x.aar") }
//        }.toList().firstNotNullOfOrNull { url ->
//            runCatching {
//                val connection = URL(url).openConnection()
//                connection.getInputStream() ?: throw Exception()
//                connection.url
//            }.getOrNull()
//        }
//    }
//tasks.register("printDependenciesURLs") {
//    doLast {
//        dependenciesURLs.forEach { (dependency: String, url: URL?) -> println("$dependency => $url") }
//    }
//}

// Can be called like: ./gradlew mergeWeblate
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
