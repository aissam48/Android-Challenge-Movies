package com.android.moviesbymoviedb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.moviesbymoviedb.sealeds.EventRepo
import com.android.moviesbymoviedb.sealeds.EventUI
import com.android.moviesbymoviedb.models.MovieModel
import com.android.moviesbymoviedb.repository.ApiServicesImpl
import com.android.moviesbymoviedb.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val apiServicesImpl: ApiServicesImpl,
) : ViewModel() {

    private val _sharedFlowFetchMovies =
        MutableSharedFlow<EventUI<List<MovieModel>>>()
    val sharedFlowFetchMovies = _sharedFlowFetchMovies.asSharedFlow()

    val listOfMovies = mutableListOf<MovieModel>()

    val favoriteMovies = mutableListOf<MovieModel>()

    var page = 1
    fun fetchMovies(s: String) {
        val params = hashMapOf<String, Any>()
        params["api_key"] = Constants.API_KEY
        params["language"] = "en-US"
        params["page"] = page
        params["query"] = s
        params["include_adult"] = false
        viewModelScope.launch(Dispatchers.IO) {

            _sharedFlowFetchMovies.emit(EventUI.OnLoading(true))
            apiServicesImpl.fetchMovies(params).onEach { result ->
                _sharedFlowFetchMovies.emit(EventUI.OnLoading(false))

                when (result) {
                    is EventRepo.Success -> {
                        if (page > 1 && result.data?.isEmpty() == true) {
                            page--
                        }
                        listOfMovies.addAll(result.data ?: listOf())
                        _sharedFlowFetchMovies.emit(EventUI.OnSuccess(listOfMovies))
                    }

                    is EventRepo.Error -> {
                        _sharedFlowFetchMovies.emit(
                            EventUI.OnError(
                                result.apiError.message,
                                result.apiError.statusCode
                            )
                        )
                    }
                }

            }.launchIn(this)
        }
    }

    fun updateMovie(movie: MovieModel) {
        viewModelScope.launch(Dispatchers.IO) {
            apiServicesImpl.updateMovie(movie)
        }
    }

    fun insertMoviesToLocalDatabase(listOfMovies: List<MovieModel>?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (listOfMovies != null) {
                apiServicesImpl.insertMoviesToLocalDatabase(listOfMovies)
            }
        }
    }


    fun fetchMoviesFromLocalDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            _sharedFlowFetchMovies.emit(EventUI.OnLoading(true))
            apiServicesImpl.fetchMoviesFromLocalDatabase().onEach { result ->
                _sharedFlowFetchMovies.emit(EventUI.OnLoading(false))
                listOfMovies.addAll((result as EventRepo.Success).data ?: listOf())
                _sharedFlowFetchMovies.emit(EventUI.OnSuccess(listOfMovies))
            }.launchIn(this)
        }
    }


    private val _sharedFlowUpdateItem =
        MutableSharedFlow<Int>()
    val sharedFlowUpdateItem = _sharedFlowUpdateItem.asSharedFlow()
    fun updateItem(model: String) {
        val updatedMovie =
            Gson().fromJson<MovieModel>(model, object : TypeToken<MovieModel>() {}.type)

        val targetItem = listOfMovies.find { it.id == updatedMovie.id } ?: return
        val targetIndex = listOfMovies.indexOf(targetItem)
        if (targetIndex != -1) {
            listOfMovies[targetIndex] = updatedMovie
            viewModelScope.launch(Dispatchers.IO) {
                _sharedFlowFetchMovies.emit(EventUI.OnSuccess(listOfMovies))
                _sharedFlowUpdateItem.emit(targetIndex)
            }

        }
    }

    private val _sharedFlowRemoveItem =
        MutableSharedFlow<Int>()
    val sharedFlowRemoveItem = _sharedFlowRemoveItem.asSharedFlow()
    fun removeFromFavorite(model: String) {
        val updatedMovie =
            Gson().fromJson<MovieModel>(model, object : TypeToken<MovieModel>() {}.type)

        if (updatedMovie.isFavorite) {
            return
        }
        val targetItem = favoriteMovies.find { it.id == updatedMovie.id } ?: return
        val targetIndex = favoriteMovies.indexOf(targetItem)
        if (targetIndex != -1) {
            favoriteMovies.removeAt(targetIndex)
            viewModelScope.launch(Dispatchers.IO) {
                _sharedFlowRemoveItem.emit(targetIndex)
            }
        }
    }

}

