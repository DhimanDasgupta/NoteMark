plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.notemark.android.compose)
}

android {
  namespace = "com.dhimandasgupta.notemark.common.presenter"
}

dependencies {
  // AppPresenter takes an AppStateMachineFactory and returns AppState.
  api(project(":common-state-event-impl"))

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)

  // junit and kotlin("test") come from the library convention plugin.
  testImplementation(testFixtures(project(":common-state-event-impl")))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.molecule)
  testImplementation(libs.turbine)
}
