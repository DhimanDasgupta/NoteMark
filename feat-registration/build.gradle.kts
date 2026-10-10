plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.notemark.android.compose)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.metro)
}

android {
  namespace = "com.dhimandasgupta.notemark.features.registration"
}

dependencies {
  // RegistrationStateMachineFactory takes a NoteMarkApi.
  api(project(":common-data"))

  implementation(project(":common-android"))
  implementation(project(":common-compose"))
  implementation(project(":common-navigation-api"))

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.bundles.compose)
  debugImplementation(libs.androidx.ui.tooling)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.flow.redux)
  implementation(libs.molecule)

  // junit and kotlin("test") come from the library convention plugin.
  testImplementation(testFixtures(project(":common-data")))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.molecule)
  testImplementation(libs.turbine)
}
