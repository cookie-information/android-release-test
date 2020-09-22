plugins {
  id(Libraries.Android.ApplicationPluginId)
  kotlin(Libraries.Kotlin.AndroidPluginId)
}

android {
  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(Libraries.Kotlin.StdLibJdk7)
  implementation(Libraries.AndroidX.Core)
  implementation(Libraries.Material.Core)

  implementation(project(":library"))
}
