package com.clearcode.mobileconsents.networking.extension

import java.io.Closeable
import java.io.IOException

@Suppress("SwallowedException", "EmptyCatchBlock")
internal fun Closeable.closeQuietly() {
  try {
    close()
  } catch (_: IOException) {
  }
}
