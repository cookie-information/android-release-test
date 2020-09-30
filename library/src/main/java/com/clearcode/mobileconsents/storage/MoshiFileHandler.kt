package com.clearcode.mobileconsents.storage

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.Sink
import okio.Source
import okio.buffer

internal class MoshiFileHandler(moshi: Moshi) {

  private val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
  private val adapter = moshi.adapter<Map<String, String>>(type)

  fun writeTo(sink: Sink, values: Map<String, String>) {
    sink.buffer().use { bufferedSink ->
      val writer = JsonWriter.of(bufferedSink)
      adapter.toJson(writer, values)
    }
  }

  fun readFrom(source: Source): Map<String, String> =
    source.buffer().use { bufferedSource ->
      if (bufferedSource.exhausted()) {
        return@use emptyMap()
      }

      val reader = JsonReader.of(bufferedSource)

      val map =
        adapter.fromJson(reader) ?: throw JsonDataException("JSON data couldn't be parsed.")

      if (reader.peek() !== JsonReader.Token.END_DOCUMENT) {
        throw JsonDataException("JSON document was not fully consumed.")
      }
      map
    }
}
