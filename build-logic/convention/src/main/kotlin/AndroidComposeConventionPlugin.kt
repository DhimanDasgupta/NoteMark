import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.skydoves.compose.stability.gradle.StabilityAnalyzerExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Compose compiler plus the stability analyzer, both reading the shared root
 * `compose-stability.conf`, and the opt-in for the experimental Compose Styles API. Apply alongside
 * an Android application or library plugin.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
      pluginManager.apply("com.github.skydoves.compose.stability.analyzer")

      pluginManager.withPlugin("com.android.application") {
        extensions.configure<ApplicationExtension> { buildFeatures.compose = true }
        optInToStyles()
      }
      pluginManager.withPlugin("com.android.library") {
        extensions.configure<LibraryExtension> { buildFeatures.compose = true }
        optInToStyles()
      }

      val stabilityConfig = rootProject.layout.projectDirectory.file("compose-stability.conf")

      extensions.configure<ComposeCompilerGradlePluginExtension> {
        stabilityConfigurationFiles.add(stabilityConfig)
      }

      extensions.configure<StabilityAnalyzerExtension> {
        stabilityConfigurationFiles.add(stabilityConfig)
      }
    }

  /** AGP's built-in Kotlin registers this extension alongside the Android plugin. */
  private fun Project.optInToStyles() {
    extensions.configure<KotlinAndroidProjectExtension> {
      compilerOptions.freeCompilerArgs.add(
        "-opt-in=androidx.compose.foundation.style.ExperimentalFoundationStyleApi"
      )
    }
  }
}
