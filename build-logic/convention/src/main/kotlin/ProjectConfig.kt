import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Shared build settings for every NoteMark module, all read from gradle/libs.versions.toml.

internal val Project.libs: VersionCatalog
  get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal val Project.compileSdk: Int
  get() = libs.intVersion("compileSdk")

internal val Project.minSdk: Int
  get() = libs.intVersion("minSdk")

internal val Project.javaVersion: JavaVersion
  get() = JavaVersion.toVersion(libs.version("jvmTarget"))

internal val Project.jvmTarget: JvmTarget
  get() = JvmTarget.fromTarget(libs.version("jvmTarget"))

private fun VersionCatalog.intVersion(alias: String): Int = version(alias).toInt()

private fun VersionCatalog.version(alias: String): String =
  findVersion(alias)
    .orElseThrow { IllegalStateException("Missing version '$alias' in gradle/libs.versions.toml") }
    .requiredVersion
