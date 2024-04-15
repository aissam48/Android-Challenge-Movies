package com.android.moviesbymoviedb.domain.sealeds

import com.android.moviesbymoviedb.domain.models.APIErrorModel


sealed class EventRepo<T>() {
    class Success<T>(val data: T?) : EventRepo<T>()
    class Error<T>(val apiErrorModel: APIErrorModel) : EventRepo<T>()
}
