package com.clearcode.mobileconsents.adapter.extension

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.JsonReader
import okhttp3.ResponseBody
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import okio.IOException

private val utfBom: ByteString = "EFBBBF".decodeHex()

internal fun <T> JsonAdapter<T>.parseResponseBody(body: ResponseBody): T =
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
