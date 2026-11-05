
package com.flagquiz.app.core.common

/**
 * Resource: The 3 Possible States of Any Operation.
 *
 * Think of ordering a package online:
 * 1. Loading: The delivery truck is on its way.
 * 2. Success: The box arrived with your items!
 * 3. Error: The truck broke down (with an error message).
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}