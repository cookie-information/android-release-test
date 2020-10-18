plugins {
  id(Libraries.Android.LibraryPluginId)
  kotlin(Libraries.Kotlin.AndroidPluginId)
  kotlin(Libraries.Kotlin.KaptPluginId)
  id(Libraries.Dokka.PluginId) version Libraries.Kotlin.Version
  id(Libraries.MavenPublish.PluginId)
}

android {
  lintOptions {
    lintConfig = rootProject.file("lint.xml")

    htmlReport = false
    xmlReport = true
    xmlOutput = rootProject.file("build/reports/lint/lint-results.xml")

    textReport = true
    textOutput("stdout")
    isExplainIssues = false

    // Full linting is run as a part of the CI so it can be skipped for the 'assemble' task.
    isCheckReleaseBuilds = false
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  api(Libraries.Kotlin.StdLibJdk7)
  api(Libraries.Okhttp.Core)
  implementation(Libraries.Moshi.Core)
  kapt(Libraries.Moshi.CodeGen)
  implementation(Libraries.Coroutines.Core)

  testImplementation(Libraries.Okhttp.MockWebServer)
  testImplementation(Libraries.Kotest.Assertions)
  testImplementation(Libraries.Kotest.RunnerJunit5)

  dokkaHtmlPlugin(Libraries.Dokka.KotlinAsJava)
}

tasks.dokkaJavadoc.configure {
  dokkaSourceSets {
    configureEach {
      skipEmptyPackages.set(true)
    }
  }
}

signing {
  val signingKey = findProperty("SONATYPE_NEXUS_SIGNING_KEY") as? String ?: ""
  if (signingKey.isNotEmpty()) {
    useInMemoryPgpKeys(signingKey, "")
  }
}
