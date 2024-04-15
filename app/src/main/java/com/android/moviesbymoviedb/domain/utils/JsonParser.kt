package com.android.moviesbymoviedb.domain.utils

import com.android.moviesbymoviedb.domain.models.MovieModel
import org.json.JSONArray

object JsonParser {

    fun getMovies(json: JSONArray): List<MovieModel> {
        val listOfMovies = mutableListOf<MovieModel>()
        for (i in 0 until json.length()) {
            listOfMovies.add(MovieModel(json.getJSONObject(i)))
        }
        return listOfMovies
    }

}

