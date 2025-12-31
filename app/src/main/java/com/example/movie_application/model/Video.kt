package com.example.movie_application.model

import com.google.gson.annotations.SerializedName

data class VideoResponse(
    val results: List<Video>
)

data class Video(
    val key: String,      // YouTube ID
    val site: String,     // მაგ: "YouTube"
    val type: String      // მაგ: "Trailer"
)