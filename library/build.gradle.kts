import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id(Libraries.Android.LibraryPluginId)
  kotlin(Libraries.Kotlin.AndroidPluginId)
  kotlin(Libraries.Kotlin.KaptPluginId)
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
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + "-Xexplicit-api=strict"
  }
}

dependencies {
  implementation(Libraries.Kotlin.StdLibJdk7)
  api(Libraries.Okhttp.Core)
  implementation(Libraries.Moshi.Core)
  kapt(Libraries.Moshi.CodeGen)
}
