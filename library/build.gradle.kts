plugins {
  id("com.android.library")
  kotlin("android")
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

dependencies {
  implementation(Libraries.Kotlin.StdLibJdk7)
}
