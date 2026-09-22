package com.chefitup.app.domain.model

/**
 * Generic async result wrapper for repository / ViewModel layers.
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(
        val messageResId: Int? = null,
        val message: String? = null,
        val cause: Throwable? = null
    ) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

/**
 * Outcome of a one-shot API / repository call (no loading state).
 */
sealed class ApiOutcome<out T> {
    data class Success<T>(val data: T) : ApiOutcome<T>()
    data class Failure(
        val messageResId: Int? = null,
        val message: String? = null,
        val cause: Throwable? = null,
        val isNetworkError: Boolean = false
    ) : ApiOutcome<Nothing>()
}

inline fun <T, R> ApiOutcome<T>.map(transform: (T) -> R): ApiOutcome<R> = when (this) {
    is ApiOutcome.Success -> ApiOutcome.Success(transform(data))
    is ApiOutcome.Failure -> this
}
