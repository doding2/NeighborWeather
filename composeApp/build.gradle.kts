
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.konfig)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.google.secrets)
}

val secretsProperties = Properties()
secretsProperties.load(rootProject.file("secrets.properties").reader())

buildkonfig {
    packageName = "com.doding2.neighborweather"
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "GOOGLE_MAPS_ANDROID_API_KEY", secretsProperties.getProperty("google_maps_android_api_key"))
    }
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    cocoapods {
        summary = "Neighbor Weather App"
        homepage = "https://github.com/doding2/NeighborWeather"
        version = "1.0.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "composeApp"
            isStatic = true
        }
        pod("GoogleMaps") {
            version = "8.4.0"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }

    // ksp
    sourceSets.commonMain {
        kotlin.srcDir("build/generated/ksp/metadata")
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // ktor client
            implementation(libs.ktor.client.okhttp)

            // koin
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            // google maps compose
            api(libs.google.maps.android.play.services)
            implementation(libs.google.maps.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // ktor
            implementation(libs.bundles.ktor)

            // koin
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // viewModel
            implementation(libs.lifecycle.viewmodel)

            // navigation
            implementation(libs.compose.navigation)
            implementation(libs.kotlinx.serialization.json)

            // datetime
            implementation(libs.datetime)

            // logger
            api(libs.kermit.logger)

            // html parser
            implementation(libs.ksoup)

            // room database
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            // permission request helper
            api(libs.moko.permissions.compose)

            // compass - location utils
            implementation(libs.compass.geolocation)
            implementation(libs.compass.geolocation.mobile)
            implementation(libs.compass.geocoder)
            implementation(libs.compass.geocoder.mobile)
        }
        nativeMain.dependencies {
            // ktor client
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.doding2"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.doding2"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

// define database schemas
room {
    schemaDirectory("$projectDir/schemas")
}

// for recognizing ksp, add another dependencies block
dependencies {
    // add room annotation compiler using ksp
    add("kspCommonMainMetadata", libs.room.compiler)
}

// for  ksp
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata" ) {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
