package com.android.moviesbymoviedb.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.android.moviesbymoviedb.domain.models.MovieModel


@Database(entities = [MovieModel::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getInstance(context: Context): MovieDatabase {
            return INSTANCE ?: Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                MovieDatabase::class.java
            ).build().also {
                INSTANCE = it
            }
        }
    }
}