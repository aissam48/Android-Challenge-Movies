package com.android.moviesbymoviedb.domain.models


import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.json.JSONObject


@Serializable
@Entity("movies_table")
data class MovieModel(
    @PrimaryKey(autoGenerate = false)
    var id: Int = -1,
    var adult: Boolean = false,
    var backdropPath: String = "",
    var firstAirDate: String = "",
    var name: String = "",
    var originalLanguage: String = "",
    var originalName: String = "",
    var overview: String = "",
    var popularity: Double = 0.0,
    var posterPath: String = "",
    var voteAverage: Double = 0.0,
    var voteCount: Int = 0,
    var isFavorite: Boolean = false
) {
    constructor(json: JSONObject) : this() {
        if (json.has("id") && !json.isNull("id")) {
            id = json.getInt("id")
        }
        if (json.has("adult") && !json.isNull("adult")) {
            adult = json.getBoolean("adult")
        }
        if (json.has("backdrop_path") && !json.isNull("backdrop_path")) {
            backdropPath = json.getString("backdrop_path")
        }
        if (json.has("first_air_date") && !json.isNull("first_air_date")) {
            firstAirDate = json.getString("first_air_date")
        }
        if (json.has("name") && !json.isNull("name")) {
            name = json.getString("name")
        }
        if (json.has("original_language") && !json.isNull("original_language")) {
            originalLanguage = json.getString("original_language")
        }
        if (json.has("original_name") && !json.isNull("original_name")) {
            originalName = json.getString("original_name")
        }
        if (json.has("overview") && !json.isNull("overview")) {
            overview = json.getString("overview")
        }
        if (json.has("popularity") && !json.isNull("popularity")) {
            popularity = json.getDouble("popularity")
        }
        if (json.has("poster_path") && !json.isNull("poster_path")) {
            posterPath = json.getString("poster_path")
        }
        if (json.has("vote_average") && !json.isNull("vote_average")) {
            voteAverage = json.getDouble("vote_average")
        }
        if (json.has("vote_count") && !json.isNull("vote_count")) {
            voteCount = json.getInt("vote_count")
        }
        if (json.has("isFavorite") && !json.isNull("isFavorite")) {
            isFavorite = json.getBoolean("isFavorite")
        }
    }
}