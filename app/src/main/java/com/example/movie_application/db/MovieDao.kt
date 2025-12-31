package com.example.movie_application.db


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Query("DELETE FROM movies_table WHERE id = :movieId")
    suspend fun deleteMovie(movieId: Int)

    @Query("SELECT * FROM movies_table")
    suspend fun getAllMovies(): List<MovieEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM movies_table WHERE id = :movieId)")
    suspend fun isMovieSaved(movieId: Int): Boolean
}