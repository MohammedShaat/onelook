package com.example.onelook.util

sealed class CustomResult<T>(val data: T? = null, val exception: Exception? = null) {
    class Success<T>(data: T) : CustomResult<T>(data)
    class Loading<T>(data: T? = null) : CustomResult<T>(data)
    class Failure<T>(exception: Exception, data: T? = null) : CustomResult<T>(data, exception)
}