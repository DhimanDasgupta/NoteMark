plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.notemark.android.compose)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.metro)
}

android {
  namespace = "com.dhimandasgupta.notemark.features.notelist"
}

dependencies {
  // NoteListPresenter takes an AppStateMachineFactory; the note list state machine takes
  // repositories.
  api(project(":common-state-event-impl"))
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
  implementation(libs.androidx.material3.adaptive.navigation3)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.flow.redux)
  implementation(libs.molecule)

  // junit and kotlin("test") come from the library convention plugin.
  testImplementation(testFixtures(project(":common-state-event-impl")))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.molecule)
  testImplementation(libs.turbine)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
}
