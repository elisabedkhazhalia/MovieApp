package com.example.movie_application.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_application.api.RetrofitInstance
import com.example.movie_application.model.Movie
import com.example.movie_application.model.MovieResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class SearchViewModel : ViewModel() {

    // 1. აი, ეს არის ის ცვლადი, რომელსაც ფრაგმენტი ეძებს!
    private val _searchResults = MutableLiveData<List<Movie>>()
    val searchResults: LiveData<List<Movie>> = _searchResults

    fun searchMovies(query: String) {
        if (query.isEmpty()) return

        Log.d("SearchDebug", "🔍 ვიწყებ ძებნას: $query")

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.searchMovies(query)
                if (response.isSuccessful) {
                    // 👇 შესწორება აქ არის!
                    // თუ results არის null, ვიყენებთ ცარიელ სიას (emptyList())
                    val movies = response.body()?.results ?: emptyList()

                    Log.d("SearchDebug", "✅ ნაპოვნია: ${movies.size} ფილმი")
                    _searchResults.postValue(movies)

                } else {
                    Log.e("SearchDebug", "❌ შეცდომა API-დან: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SearchDebug", "💥 პროგრამული შეცდომა (Crash): ${e.message}")
            }
        }
    }
}