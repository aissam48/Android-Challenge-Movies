package com.android.moviesbymoviedb.data.remote

import com.android.moviesbymoviedb.domain.repository.sealed.EventRepo
import com.android.moviesbymoviedb.domain.models.MovieModel
import kotlinx.coroutines.flow.Flow

interface ApiServices {

    suspend fun fetchMovies(params:HashMap<String, Any>):Flow<EventRepo<List<MovieModel>>>
    suspend fun insertMoviesToLocalDatabase(movies:List<MovieModel>)
    suspend fun fetchMoviesFromLocalDatabase():Flow<EventRepo<List<MovieModel>>>
    suspend fun updateMovie(movie: MovieModel)
}