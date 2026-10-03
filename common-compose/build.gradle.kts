plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.notemark.android.compose)
}

android {
  namespace = "com.dhimandasgupta.notemark.common.compose"
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  api(libs.androidx.material3)
  api(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  debugImplementation(libs.androidx.ui.tooling)
  implementation(libs.androidx.material3.adaptive)
  implementation(libs.androidx.lifecycle.runtime.compose)
}
