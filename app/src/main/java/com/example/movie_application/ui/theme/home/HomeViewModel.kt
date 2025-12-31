package com.example.movie_application.ui.theme.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_application.api.RetrofitInstance
import com.example.movie_application.model.Movie
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> = _movies

    init {
        getPopularMovies()
    }

    // 1. პოპულარული ფილმების წამოღება
    fun getPopularMovies() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getTrendingMovies()
                if (response.isSuccessful) {
                    _movies.postValue(response.body()?.results)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error: ${e.message}")
            }
        }
    }

    // 2. ჟანრის მიხედვით წამოღება (ეს დაამატე!)
    fun getMoviesByGenre(genreId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMoviesByGenre(genreId)
                if (response.isSuccessful) {
                    _movies.postValue(response.body()?.results)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Genre Error: ${e.message}")
            }
        }
    }
}