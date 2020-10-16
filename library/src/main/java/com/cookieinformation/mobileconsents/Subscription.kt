package com.cookieinformation.mobileconsents

/**
 * Interface returned from every async operation of SDK, use it for cancellation of background operations.
 */
public interface Subscription {
  /**
   * Cancel ongoing background operation.
   */
  public fun cancel()
}
