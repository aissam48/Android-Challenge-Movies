package com.android.moviesbymoviedb.data

import android.app.Application
import android.content.Context
import android.util.Log
import com.android.moviesbymoviedb.domain.sealeds.EventRepo
import com.android.moviesbymoviedb.domain.models.MovieModel
import com.android.moviesbymoviedb.domain.utils.JsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import javax.inject.Inject
import kotlin.collections.HashMap

class ApiServicesImpl @Inject constructor(
    private val apiManager: ApiManager
) : ApiServices {

    @Inject
    @ApplicationContext
    lateinit var appContext: Context

    @Inject
    lateinit var application: Application

    @Inject
    lateinit var movieDatabase: MovieDatabase

    override suspend fun fetchMovies(params: HashMap<String, Any>): Flow<EventRepo<List<MovieModel>>> =
        flow {

            val parameters = arrayListOf<Pair<String, Any>>()

            for ((key, value) in params) {
                parameters.add(Pair(key, value))
            }

            apiManager.makeRequest(
                url = BaseUrl.URL.value,
                bodyMap = null,
                reqMethod = HttpMethod.Get,
                parameterFormData = parameters,
                failureCallback = { error ->
                    emit(EventRepo.Error(error.apiErrorModel))
                },
                successCallback = {
                    var resultsArray = JSONArray()

                    if (it.has("results")) {
                        resultsArray = it.getJSONArray("results")
                    }


                    emit(EventRepo.Success(JsonParser.getMovies(resultsArray)))
                }
            )
        }

    override suspend fun insertMoviesToLocalDatabase(movies: List<MovieModel>) {
        movieDatabase.movieDao().insertMovies(movies)

    }

    override suspend fun fetchMoviesFromLocalDatabase(): Flow<EventRepo<List<MovieModel>>> =
        flow {

            var movies = listOf<MovieModel>()
            movies = movieDatabase.movieDao().getAllMovies()

            emit(EventRepo.Success(movies))
        }

    override suspend fun updateMovie(movie: MovieModel) {
        movieDatabase.movieDao().updateMovie(movie)

    }

}