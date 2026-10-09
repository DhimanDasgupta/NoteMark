plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.notemark.android.compose)
  alias(libs.plugins.kotlinx.serialization)
}

android {
  namespace = "com.dhimandasgupta.notemark.common.navigation"
}

dependencies {
  // NavKey is part of this module's public API.
  api(libs.androidx.navigation3.runtime)
  implementation(libs.kotlinx.serialization.core)
}
