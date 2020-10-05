plugins {
  id(Libraries.Android.ApplicationPluginId)
  kotlin(Libraries.Kotlin.AndroidPluginId)
  kotlin(Libraries.Kotlin.AndroidExtensionsPluginId)
}

android {
  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(Libraries.Kotlin.StdLibJdk7)
  implementation(Libraries.AndroidX.Core)
  implementation(Libraries.Android.CoreKtx)
  implementation(Libraries.Design.Material)
  implementation(Libraries.Design.ConstraintLayout)

  implementation(project(":library"))
}
