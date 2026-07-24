package com.example.daggerpokedex.domain.util

/**
 * A tiny result wrapper used across layers to model the three states of an
 * asynchronous operation: success, failure, and (optionally) loading.
 *
 * Keeping this in the domain layer means both `data` and `presentation` can
 * speak the same language about outcomes without leaking Retrofit/HTTP details.
 */
sealed interface Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>
}
