package com.chefitup.app.util

import com.chefitup.app.R
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorMapper {

    fun messageResId(throwable: Throwable): Int = when (throwable) {
        is UnknownHostException -> R.string.error_no_internet
        is SocketTimeoutException -> R.string.error_timeout
        is IOException -> R.string.error_network
        is HttpException -> when (throwable.code()) {
            401, 403 -> R.string.error_unauthorized
            404 -> R.string.error_not_found
            429 -> R.string.error_rate_limit
            in 500..599 -> R.string.error_server
            else -> R.string.error_generic
        }
        else -> R.string.error_generic
    }

    fun isNetworkError(throwable: Throwable): Boolean =
        throwable is IOException ||
            (throwable is HttpException && throwable.code() >= 500)
}
