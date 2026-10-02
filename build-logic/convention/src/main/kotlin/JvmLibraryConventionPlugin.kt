import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** Pure Kotlin/JVM library: no Android dependencies, linted by Android Lint. */
class JvmLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      pluginManager.apply("org.jetbrains.kotlin.jvm")
      pluginManager.apply("com.android.lint")

      // Read from the catalog up front: inside `compilerOptions` `jvmTarget` is the option itself.
      val javaVersionTarget = javaVersion
      val kotlinJvmTarget = jvmTarget

      extensions.configure<JavaPluginExtension> {
        sourceCompatibility = javaVersionTarget
        targetCompatibility = javaVersionTarget
      }

      extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions { jvmTarget.set(kotlinJvmTarget) }
      }

      dependencies {
        add("testImplementation", libs.findLibrary("junit").get())
        add("testImplementation", kotlin("test"))
      }
    }
}
