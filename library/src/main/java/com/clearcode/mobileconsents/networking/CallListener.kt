package com.clearcode.mobileconsents.networking

public interface CallListener<in T> {
  public fun onSuccess(result: T)
  public fun onFailure(error: Throwable)
}
