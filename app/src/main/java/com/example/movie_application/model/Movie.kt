package com.example.movie_application.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Movie(
    val id: Int,

    val title: String?,

    @SerializedName("poster_path")
    val posterPath: String?,

    val overview: String?,

    @SerializedName("release_date")
    val releaseDate: String?,

    @SerializedName("vote_average")
    val rating: Double?
) : Parcelable