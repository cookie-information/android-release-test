package com.clearcode.mobileconsents.networking

import java.io.IOException

/**
 * Callback used for all async operations in SDK.
 */
public interface CallListener<in T> {
  /**
   * Retrieves successful result of async operation.
   * @return result of async operation.
   */
  public fun onSuccess(result: T)

  /**
   * Retrieves failure of async operation.
   * @return [IOException]
   */
  public fun onFailure(error: IOException)
}
