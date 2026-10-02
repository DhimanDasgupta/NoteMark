plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.notemark.android.compose)
  alias(libs.plugins.kotlinx.serialization)
}

android {
  namespace = "com.dhimandasgupta.notemark.common.android"
}

dependencies {
  api(project(":common-core"))

  implementation(libs.androidx.core.ktx)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
}
