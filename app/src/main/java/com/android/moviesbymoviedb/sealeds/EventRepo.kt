package com.android.moviesbymoviedb.sealeds

import com.android.moviesbymoviedb.models.APIError


sealed class EventRepo<T>() {
    class Success<T>(val data: T?) : EventRepo<T>()
    class Error<T>(val apiError: APIError) : EventRepo<T>()
}
