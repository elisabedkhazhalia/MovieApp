package com.example.movie_application.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies_table")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val posterPath: String?,
    val rating: Double,
    val releaseDate: String?,
    val overview: String
)