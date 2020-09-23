package com.clearcode.mobileconsents.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.UUID

internal val uuidAdapter = object {
  @FromJson
  fun fromJson(uuid: String) = UUID.fromString(uuid)

  @ToJson
  fun toJson(uuid: UUID) = uuid.toString()
}
