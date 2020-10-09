include(":sample")
include(":library")
project(file("library")).name = "Mobile Consents"
pluginManagement {
  repositories {
    gradlePluginPortal()
    jcenter()
  }
}
