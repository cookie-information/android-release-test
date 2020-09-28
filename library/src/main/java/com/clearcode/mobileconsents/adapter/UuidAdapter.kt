package com.clearcode.mobileconsents.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson
import java.util.UUID

internal val uuidAdapter = object {
  @FromJson
  fun fromJson(uuid: String) = try {
    UUID.fromString(uuid)
  } catch (e: IllegalArgumentException) {
    throw JsonDataException("Couldn't parse UUID: $uuid", e)
  }

  @ToJson
  fun toJson(uuid: UUID) = uuid.toString()
}
