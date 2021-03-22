plugins {
  id(Libraries.Android.LibraryPluginId)
  kotlin(Libraries.Kotlin.AndroidPluginId)
  kotlin(Libraries.Kotlin.KaptPluginId)
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
  api(Libraries.Kotlin.StdLib)
  api(Libraries.Okhttp.Core)
  implementation(Libraries.AndroidX.AppCompat)
  implementation(Libraries.AndroidX.ConstraintLayout)
  implementation(Libraries.AndroidX.Fragment)
  implementation(Libraries.AndroidX.RecyclerView)
  implementation(Libraries.Moshi.Core)
  kapt(Libraries.Moshi.CodeGen)
  implementation(Libraries.Coroutines.Core)

  testImplementation(Libraries.Okhttp.MockWebServer)
  testImplementation(Libraries.Kotest.Assertions)
  testImplementation(Libraries.Kotest.RunnerJunit5)
  testImplementation(Libraries.MockK.Core)
}

signing {
  val signingKey = findProperty("SONATYPE_NEXUS_SIGNING_KEY") as? String ?: ""
  val password = findProperty("SONATYPE_NEXUS_SIGNING_KEY_PASSWORD") as? String ?: ""
  if (signingKey.isNotEmpty() && password.isNotEmpty()) {
    useInMemoryPgpKeys(signingKey, password)
  }
}
