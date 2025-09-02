package com.trackloan.common

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

// Extension functions for Result
inline fun <T, R> Result<T>.fold(
    onSuccess: (T) -> R,
    onError: (Exception) -> R,
    onLoading: () -> R = { throw IllegalStateException("Loading state should not be folded") }
): R = when (this) {
    is Result.Success -> onSuccess(data)
    is Result.Error -> onError(exception)
    is Result.Loading -> onLoading()
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (Exception) -> Unit): Result<T> {
    if (this is Result.Error) action(exception)
    return this
}

inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> {
    if (this is Result.Loading) action()
    return this
}

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Error -> null
    is Result.Loading -> null
}

fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Error -> throw exception
    is Result.Loading -> throw IllegalStateException("Result is still loading")
}

fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success
fun <T> Result<T>.isError(): Boolean = this is Result.Error
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading
