import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.google.protobuf)
}

// Create a Properties object to hold our values
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

// Load the properties if the file exists
if (localPropertiesFile.exists() && localPropertiesFile.isFile) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.dhimandasgupta.notemark"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.dhimandasgupta.notemark"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val headerValue = localProperties.getProperty("HEADER_VALUE_FOR_NOTE_MARK_API", "")
        buildConfigField("String", "HEADER_VALUE_FOR_NOTE_MARK_API", "\"$headerValue\"")

        buildTypes {
            getByName("debug") {
                buildConfigField("boolean", "DEBUGGABLE", "true")
            }
            getByName("release") {
                buildConfigField("boolean", "DEBUGGABLE", "false")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    val javaVersion = JavaVersion.VERSION_17
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {}
    kotlin {
        jvmToolchain(javaVersion.toString().toInt())
    }
    buildFeatures {
        compose = true
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

sqldelight {
    databases {
        create("NoteMarkDatabase") {
            packageName.set("com.dhimandasgupta.notemark.database")
        }
    }
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("kotlin") {
                    option("lite")
                }
                register("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3.window.size.android)
    implementation(libs.androidx.work.runtime.ktx)
    coreLibraryDesugaring(libs.desugar.jdk)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.splash.screen)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.bundles.compose)
    debugImplementation(libs.bundles.compose.debug)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Koin
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.work.manager)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)

    // Flow Redux
    implementation(libs.flow.redux.jvm)

    // Molecule
    implementation(libs.molecule)

    // Kotlinx Serialization JSON
    implementation(libs.kotlinx.serialization.json)

    // SQL Delight
    implementation(libs.sql.delight.runtime)
    implementation(libs.sql.delight.android.driver)
    implementation(libs.sql.delight.coroutines.extensions)

    // Datastore
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.preferences.android)

    // Ktor
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)

    // Kotlinx collections
    implementation(libs.kotlinx.collections.immutable)

    // Protobuf
    implementation(libs.kotlin.protobuf)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(kotlin("test"))
}