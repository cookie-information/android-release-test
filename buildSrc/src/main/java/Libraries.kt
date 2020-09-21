@Suppress("ObjectPropertyNaming") // Makes sense here due to libraries coordinates.
object Libraries {
  const val AndroidGradlePlugin = "com.android.tools.build:gradle:4.0.1"

  object Kotlin {
    const val Version = "1.4.10"

    const val GradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$Version"

    const val StdLibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$Version"
  }

  object Kotest {
    const val Version = "4.2.3"

    const val RunnerJunit5 = "io.kotest:kotest-runner-junit5-jvm:$Version"

    const val Assertions = "io.kotest:kotest-assertions-core-jvm:$Version"

    const val Property = "io.kotest:kotest-property-jvm:$Version"
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
}
