package com.android.moviesbymoviedb.data

import androidx.room.*
import com.android.moviesbymoviedb.domain.models.MovieModel


@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieModel>)

    @Query("SELECT * FROM movies_table")
    suspend fun getAllMovies(): List<MovieModel>

    @Update
    suspend fun updateMovie(movies: MovieModel)
}