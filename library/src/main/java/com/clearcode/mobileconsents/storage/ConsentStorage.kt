package com.clearcode.mobileconsents.storage

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import java.util.UUID

private const val userIdKey = "user_id_key"
private const val scratchFileSuffix = ".tmp"

// TODO Handle corruption of data
internal class ConsentStorage(
  private val mutex: Mutex,
  private val file: File,
  private val fileHandler: MoshiFileHandler,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

  init {
    createStorageFile(file)
  }

  suspend fun storeConsentChoice(consentId: UUID, choice: Boolean) =
    writeValue(consentId.toString(), choice.toString())

  suspend fun getUserId(): UUID {
    val userId = readValue(userIdKey)
    return if (userId == null) {
      val newUserId = UUID.randomUUID()
      writeValue(userIdKey, newUserId.toString())
      newUserId
    } else {
      UUID.fromString(userId)
    }
  }

  suspend fun getConsentChoice(consentId: UUID): Boolean {
    val value = readValue(consentId.toString())

    return value.toBoolean()
  }

  suspend fun getAllConsentChoices(): Map<UUID, Boolean> =
    readAll()
      .filterKeys { it != userIdKey }
      .entries.associate { UUID.fromString(it.key) to it.value.toBoolean() }

  private suspend fun writeValue(key: String, value: String) = withContext(dispatcher) {
    mutex.withLock {
      val scratchFile = File(file.path + scratchFileSuffix)

      try {
        val oldData = readAll().toMutableMap()
        oldData[key] = value

        scratchFile.sink().use { sink ->
          fileHandler.writeTo(sink, oldData)
        }

        if (!scratchFile.renameTo(file)) {
          throw IOException("$scratchFile could not be renamed to $file")
        }
      } catch (e: IOException) {
        scratchFile.delete()
        throw e
      }
    }
  }

  private suspend fun readValue(key: String): String? {
    val data = readAll()
    return data[key]
  }

  private suspend fun readAll(): Map<String, String> = withContext(dispatcher) {
    file.source().use(fileHandler::readFrom)
  }

  private fun createStorageFile(file: File) {
    file.parentFile?.mkdirs()
    if (!file.exists()) {
      file.createNewFile()
    }
  }
}
