package com.clearcode.mobileconsents.system

import com.clearcode.mobileconsents.networking.request.ApplicationPropertiesRequest

/**
 * Object representing platform and application information, required when posting consents to partner server
 */
internal data class ApplicationProperties(
  val operatingSystem: String,
  val applicationId: String,
  val applicationName: String
)

internal fun ApplicationProperties.toRequest() = ApplicationPropertiesRequest(
  operatingSystem = operatingSystem,
  applicationId = applicationId,
  applicationName = applicationName
)
