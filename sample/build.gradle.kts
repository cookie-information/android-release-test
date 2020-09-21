plugins {
  id(Libraries.Android.ApplicationPluginId)
  kotlin(Libraries.Kotlin.AndroidPluginId)
}

dependencies {
  implementation(Libraries.Kotlin.StdLibJdk7)
  implementation(Libraries.AndroidX.Core)
  implementation(Libraries.Material.Core)

  implementation(project(":library"))
}
