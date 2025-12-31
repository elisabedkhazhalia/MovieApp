package com.example.movie_application.model

data class MovieResponse(
    val page: Int,
    val results: List<Movie>
)