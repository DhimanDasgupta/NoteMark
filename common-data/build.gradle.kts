plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.sqlDelight)
  alias(libs.plugins.google.protobuf)
  alias(libs.plugins.metro)
}

android {
  namespace = "com.dhimandasgupta.notemark.data"
}

sqldelight {
  databases {
    create("NoteMarkDatabase") {
      packageName.set("com.dhimandasgupta.notemark.database")
      generateAsync.set(true)
    }
  }
}

protobuf {
  protoc {
    artifact = libs.protobuf.protoc.get().toString()
  }
  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        register("kotlin") {
          option("lite")
        }
        register("java") {
          option("lite")
        }
      }
    }
  }
}

dependencies {
  // Types exposed in this module's public API (repositories, data sources, DI bindings).
  api(project(":common-core"))
  api(libs.kotlin.protobuf)
  api(libs.sql.delight.runtime)
  api(libs.sql.delight.async.extensions)
  api(libs.androidx.paging.common)
  api(libs.datastore.core)
  api(platform(libs.ktor.bom))
  api(libs.ktor.client.core)
  api(libs.ktor.client.auth)
  api(libs.kotlinx.serialization.json)

  implementation(libs.sql.delight.coroutines.extensions)
  implementation(libs.sql.delight.paging3.extensions)

  // junit and kotlin("test") come from the library convention plugin.
  testImplementation(libs.kotlinx.coroutines.test)
}
