import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
}

group = "com.dhimandasgupta.notemark.buildlogic"

val jvmTargetVersion = JavaVersion.VERSION_21

java {
  sourceCompatibility = JavaVersion.toVersion(jvmTargetVersion)
  targetCompatibility = JavaVersion.toVersion(jvmTargetVersion)
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
  }
}

dependencies {
  implementation(libs.android.gradlePlugin)
  implementation(libs.kotlin.gradlePlugin)
  implementation(libs.compose.gradlePlugin)
  implementation(libs.compose.stability.analyser.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("jvmLibrary") {
      id = libs.plugins.notemark.jvm.library.get().pluginId
      implementationClass = "com.dhimandasgupta.notemark.convention.JvmLibraryConventionPlugin"
    }
    register("androidApplication") {
      id = libs.plugins.notemark.android.application.get().pluginId
      implementationClass =
        "com.dhimandasgupta.notemark.convention.AndroidApplicationConventionPlugin"
    }
    register("androidLibrary") {
      id = libs.plugins.notemark.android.library.get().pluginId
      implementationClass = "com.dhimandasgupta.notemark.convention.AndroidLibraryConventionPlugin"
    }
    register("androidCompose") {
      id = libs.plugins.notemark.android.compose.get().pluginId
      implementationClass = "com.dhimandasgupta.notemark.convention.AndroidComposeConventionPlugin"
    }
  }
}
