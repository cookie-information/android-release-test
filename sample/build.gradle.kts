plugins {
  id(Libraries.Android.ApplicationPluginId)
  kotlin(Libraries.Kotlin.AndroidPluginId)
  kotlin(Libraries.Kotlin.AndroidExtensionsPluginId)
}

android {
  signingConfigs {
    register("sample") {
      keyAlias = "key0"
      keyPassword = "key-password"
      storeFile = file("sample.keystore")
      storePassword = "store-password"
    }
  }
  defaultConfig {
    signingConfig = signingConfigs.getByName("sample")
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(Libraries.Kotlin.StdLib)
  implementation(Libraries.AndroidX.AppCompat)
  implementation(Libraries.AndroidX.Core)
  implementation(Libraries.AndroidX.ConstraintLayout)
  implementation(Libraries.Design.Material)
  debugImplementation(Libraries.Chucker.Core)
  implementation(project(":Mobile Consents"))
}
