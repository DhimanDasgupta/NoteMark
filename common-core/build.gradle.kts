plugins {
  alias(libs.plugins.notemark.jvm.library)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.metro)
}

dependencies {
  api(libs.kotlinx.coroutines.core)
  api(libs.kotlinx.datetime)
  api(libs.kotlinx.collections.immutable)
  api(libs.kotlinx.serialization.core)

  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.turbine)
}
