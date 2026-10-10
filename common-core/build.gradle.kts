plugins {
  alias(libs.plugins.notemark.jvm.library)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.metro)
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.collections.immutable)
  implementation(libs.kotlinx.serialization.core)

  testImplementation(libs.kotlinx.coroutines.test)
}
