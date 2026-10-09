import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
}

group = "com.dhimandasgupta.notemark.buildlogic"

val jvmTargetVersion = libs.versions.jvmTarget.get()

java {
  sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
  targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
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
