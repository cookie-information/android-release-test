package com.clearcode.mobileconsents.adapter.extension

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import okio.IOException

private const val jsonMediaType = "application/json"
private val utfBom: ByteString = "EFBBBF".decodeHex()

internal fun <T> JsonAdapter<T>.parseFromResponseBody(body: ResponseBody): T =
  try {
    body.source().use { source ->
      if (source.rangeEquals(0, utfBom)) {
        source.skip(utfBom.size.toLong())
      }
      val reader = JsonReader.of(source)
      val result = fromJson(reader) ?: throw JsonDataException("Couldn't parse the response.")
      if (reader.peek() !== JsonReader.Token.END_DOCUMENT) {
        throw JsonDataException("JSON document was not fully consumed.")
      }
      result
    }
  } catch (e: JsonDataException) {
    throw IOException(e.message, e)
  } catch (e: JsonEncodingException) {
    throw IOException(e.message, e)
  }

internal fun <T> JsonAdapter<T>.parseToRequestBody(data: T): RequestBody {
  val buffer = Buffer()
  val writer = JsonWriter.of(buffer)
  toJson(writer, data)
  return buffer.readByteString().toRequestBody(jsonMediaType.toMediaType())
}
