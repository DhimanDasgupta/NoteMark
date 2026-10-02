import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.skydoves.compose.stability.gradle.StabilityAnalyzerExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * Compose compiler plus the stability analyzer, both reading the shared root
 * `compose-stability.conf`. Apply alongside an Android application or library plugin.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
      pluginManager.apply("com.github.skydoves.compose.stability.analyzer")

      pluginManager.withPlugin("com.android.application") {
        extensions.configure<ApplicationExtension> { buildFeatures.compose = true }
      }
      pluginManager.withPlugin("com.android.library") {
        extensions.configure<LibraryExtension> { buildFeatures.compose = true }
      }

      val stabilityConfig = rootProject.layout.projectDirectory.file("compose-stability.conf")

      extensions.configure<ComposeCompilerGradlePluginExtension> {
        stabilityConfigurationFiles.add(stabilityConfig)
      }

      extensions.configure<StabilityAnalyzerExtension> {
        stabilityConfigurationFiles.add(stabilityConfig)
      }
    }
}
