package com.example.movie_application.model

import com.google.gson.annotations.SerializedName

data class PersonDetails(
    val id: Int,
    val name: String,
    val biography: String,
    val birthday: String?,
    @SerializedName("place_of_birth")
    val placeOfBirth: String?,
    @SerializedName("profile_path")
    val profilePath: String?
)