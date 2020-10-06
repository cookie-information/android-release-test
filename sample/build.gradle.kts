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
  implementation(Libraries.Kotlin.StdLibJdk7)
  implementation(Libraries.AndroidX.Core)
  implementation(Libraries.Android.CoreKtx)
  implementation(Libraries.Design.Material)
  implementation(Libraries.Design.ConstraintLayout)
  implementation(Libraries.Chucker.Core)
  implementation(project(":library"))
}
