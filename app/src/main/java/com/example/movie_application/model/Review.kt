package com.example.movie_application.model

data class ReviewResponse(
    val results: List<Review>
)

data class Review(
    val author: String,
    val content: String,
    val id: String
)