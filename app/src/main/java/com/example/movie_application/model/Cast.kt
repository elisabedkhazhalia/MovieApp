package com.example.movie_application.model

import com.google.gson.annotations.SerializedName

data class CreditsResponse(
    val cast: List<Cast>
)

data class Cast(
    val id: Int,
    val name: String,
    val character: String,
    @SerializedName("profile_path")
    val profilePath: String?
)