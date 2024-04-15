package com.android.moviesbymoviedb.domain.models


data class APIErrorModel(
    var statusCode: Int = -1,
    var message: String = "",
)

