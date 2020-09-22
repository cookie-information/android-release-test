// Makes sense here due to libraries coordinates.
@file:Suppress("ObjectPropertyNaming")

object Libraries {
  object Android {
    const val GradlePlugin = "com.android.tools.build:gradle:4.0.1"

    const val ApplicationPluginId = "com.android.application"

    const val LibraryPluginId = "com.android.library"
  }

  object Kotlin {
    const val Version = "1.4.10"

    const val GradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$Version"

    const val StdLibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$Version"

    const val AndroidPluginId = "android"

    const val KaptPluginId = "kapt"
  }

  object Kotest {
    const val Version = "4.2.3"

    const val RunnerJunit5 = "io.kotest:kotest-runner-junit5-jvm:$Version"

    const val Assertions = "io.kotest:kotest-assertions-core-jvm:$Version"
  }

  object Detekt {
    const val Version = "1.13.1"

    const val GradlePluginId = "io.gitlab.arturbosch.detekt"

    const val GradlePlugin = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$Version"

    const val Formatting = "io.gitlab.arturbosch.detekt:detekt-formatting:$Version"

    const val Cli = "io.gitlab.arturbosch.detekt:detekt-cli:$Version"
  }

  object GradleVersions {
    const val Version = "0.33.0"

    const val GradlePluginId = "com.github.ben-manes.versions"

    const val GradlePlugin = "com.github.ben-manes:gradle-versions-plugin:$Version"
  }

  object Okhttp {
    const val Version = "4.9.0"

    const val Core = "com.squareup.okhttp3:okhttp:$Version"

    const val MockWebServer = "com.squareup.okhttp3:mockwebserver:$Version"
  }

  object Moshi {
    const val Version = "1.9.3"

    const val Core = "com.squareup.moshi:moshi:$Version"

    const val CodeGen = "com.squareup.moshi:moshi-kotlin-codegen:$Version"
  }

  @Suppress("ClassNaming")
  object AndroidX {
    const val Version = "1.3.1"

    const val Core = "androidx.core:core-ktx:$Version"
  }

  object Material {
    const val Version = "1.2.1"

    const val Core = "com.google.android.material:material:$Version"
  }
}
