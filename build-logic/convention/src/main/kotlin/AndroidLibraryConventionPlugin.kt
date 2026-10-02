import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/** Android library with the app's SDK levels and Java/Kotlin targets. */
class AndroidLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      pluginManager.apply("com.android.library")

      // Read from the catalog up front: inside the DSL blocks these names resolve to the
      // extensions' own properties (e.g. `compilerOptions.jvmTarget`).
      val compileSdkLevel = compileSdk
      val minSdkLevel = minSdk
      val javaVersionTarget = javaVersion
      val kotlinJvmTarget = jvmTarget
      extensions.configure<LibraryExtension> {
        compileSdk = compileSdkLevel
        defaultConfig {
          minSdk = minSdkLevel
          consumerProguardFiles("consumer-rules.pro")
        }
        compileOptions {
          sourceCompatibility = javaVersionTarget
          targetCompatibility = javaVersionTarget
        }
        testOptions { unitTests.isReturnDefaultValues = true }
      }

      extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions { jvmTarget.set(kotlinJvmTarget) }
      }

      dependencies {
        add("testImplementation", libs.findLibrary("junit").get())
        add("testImplementation", kotlin("test"))
      }
    }
}
