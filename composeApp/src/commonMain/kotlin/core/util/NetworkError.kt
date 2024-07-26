package core.util

import core.util.NetworkError.CONFLICT
import core.util.NetworkError.PAYLOAD_TOO_LARGE
import core.util.NetworkError.REQUEST_TIMEOUT
import core.util.NetworkError.SERVER_ERROR
import core.util.NetworkError.TOO_MANY_REQUESTS
import core.util.NetworkError.UNAUTHORIZED
import core.util.NetworkError.UNKNOWN

enum class NetworkError : Error {
    REQUEST_TIMEOUT,
    UNAUTHORIZED,
    CONFLICT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    PAYLOAD_TOO_LARGE,
    SERVER_ERROR,
    SERIALIZATION,
    UNKNOWN;
}

fun Int.toNetworkError(): NetworkError {
    return when (this) {
        401 -> UNAUTHORIZED
        408 -> REQUEST_TIMEOUT
        409 -> CONFLICT
        413 -> PAYLOAD_TOO_LARGE
        429 -> TOO_MANY_REQUESTS
        in 500..599 -> SERVER_ERROR
        else -> UNKNOWN
    }
}