import java.io.FileInputStream
import java.io.FileNotFoundException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.notemark.android.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.metro)
}

// Create a Properties object to hold our values
private val localProperties = Properties()
private val localPropertiesFile: File = rootProject.file("local.properties")

// Load the properties if the file exists
if (localPropertiesFile.exists() && localPropertiesFile.isFile) {
  localProperties.load(FileInputStream(localPropertiesFile))
} else {
  throw FileNotFoundException(
    "local.properties file not found, should contain the header values HEADER_VALUE_FOR_NOTE_MARK_API"
  )
}

private val applicationId = "com.dhimandasgupta.notemark"
private val versionNamePrefix = "1.0"

// Function to generate the dynamic version name
private fun generateVersionName(): String {
  val now = LocalDateTime.now()
  val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm")
  return "$versionNamePrefix-${now.format(formatter)}"
}

private val jvmTargetVersion = libs.versions.jvmTarget.get()

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion))
  }
}

android {
  namespace = applicationId
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    applicationId = applicationId
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    versionCode = 1
    versionName = generateVersionName()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    val headerValue = localProperties.getProperty("HEADER_VALUE_FOR_NOTE_MARK_API", "")
    buildConfigField("String", "HEADER_VALUE_FOR_NOTE_MARK_API", "\"$headerValue\"")
  }

  buildTypes {
    getByName("debug") {
      buildConfigField("Boolean", "DEBUGGABLE", "true")
      isMinifyEnabled = false
    }
    getByName("release") {
      optimization {
        enable = true
      }
      buildConfigField("Boolean", "DEBUGGABLE", "false")
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(jvmTargetVersion)
    targetCompatibility = JavaVersion.toVersion(jvmTargetVersion)
  }

  buildFeatures {
    buildConfig = true
  }

  testOptions {
    unitTests.isReturnDefaultValues = true
  }

  lint {
    // One consolidated report that also covers the library modules.
    checkDependencies = true
  }
}

dependencies {
  implementation(project(":common-core"))
  implementation(project(":common-android"))
  implementation(project(":common-compose"))
  implementation(project(":common-data"))
  // The Metro graph builds the presenters, which take an AppStateMachineFactory.
  implementation(project(":common-state-event-api"))
  implementation(project(":common-state-event-impl"))
  implementation(project(":common-navigation-api"))
  implementation(project(":feat-launcher"))
  implementation(project(":feat-login"))
  implementation(project(":feat-registration"))
  implementation(project(":feat-notelist"))
  implementation(project(":feat-addnote"))
  implementation(project(":feat-editnote"))
  implementation(project(":feat-settings"))

  implementation(libs.androidx.work.runtime.ktx)
  coreLibraryDesugaring(libs.desugar.jdk)

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.splash.screen)

  // Jetpack Compose
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)
  debugImplementation(composeBom)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.ui)
  debugImplementation(libs.bundles.compose.debug)
  implementation(libs.androidx.performance.tracing)
  androidTestImplementation(libs.androidx.ui.test.junit4)

  // Flow Redux
  implementation(libs.flow.redux)

  // Kotlinx Coroutines
  implementation(libs.kotlinx.coroutines.core)

  // Kotlinx Serialization JSON
  implementation(libs.kotlinx.serialization.json)

  // SQL Delight
  implementation(libs.sql.delight.runtime)
  implementation(libs.sql.delight.async.extensions)
  implementation(libs.sql.delight.android.driver)

  // Datastore
  implementation(libs.datastore.preferences)

  // Ktor
  implementation(platform(libs.ktor.bom))
  implementation(libs.ktor.client.core)
  implementation(libs.ktor.client.android)
  implementation(libs.ktor.client.contentNegotiation)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.ktor.client.logging)
  implementation(libs.ktor.client.auth)

  // Kotlinx collections
  implementation(libs.kotlinx.collections.immutable)

  // Nav3
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.material3.adaptive.navigation3)

  implementation(libs.timber)

  // Protobuf
  implementation(libs.kotlin.protobuf)

  // Test dependencies
  testImplementation(testFixtures(project(":common-data")))
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  testImplementation(kotlin("test"))
}
