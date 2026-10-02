import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
}

group = "com.dhimandasgupta.notemark.buildlogic"

val jvmTargetVersion = libs.versions.jvmTarget.get()

java {
  sourceCompatibility = JavaVersion.toVersion(jvmTargetVersion)
  targetCompatibility = JavaVersion.toVersion(jvmTargetVersion)
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion))
  }
}

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.compose.gradlePlugin)
  compileOnly(libs.compose.stability.analyser.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("jvmLibrary") {
      id = libs.plugins.notemark.jvm.library.get().pluginId
      implementationClass = "JvmLibraryConventionPlugin"
    }
    register("androidLibrary") {
      id = libs.plugins.notemark.android.library.get().pluginId
      implementationClass = "AndroidLibraryConventionPlugin"
    }
    register("androidCompose") {
      id = libs.plugins.notemark.android.compose.get().pluginId
      implementationClass = "AndroidComposeConventionPlugin"
    }
  }
}
