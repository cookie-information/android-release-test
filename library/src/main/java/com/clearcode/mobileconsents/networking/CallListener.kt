package com.clearcode.mobileconsents.networking

import java.io.IOException

public interface CallListener<in T> {
  public fun onSuccess(result: T)
  public fun onFailure(error: IOException)
}
