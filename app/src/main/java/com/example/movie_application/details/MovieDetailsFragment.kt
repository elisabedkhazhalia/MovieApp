package com.example.movie_application.ui.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.movie_application.R
import com.example.movie_application.adapters.CastAdapter
import com.example.movie_application.adapters.MovieAdapter
import com.example.movie_application.adapters.ReviewAdapter // 👈 Added Import
import com.example.movie_application.api.Constants.IMAGE_BASE_URL
import com.example.movie_application.api.RetrofitInstance
import com.example.movie_application.databinding.FragmentMovieDetailsBinding
import com.example.movie_application.db.MovieDatabase
import com.example.movie_application.db.MovieEntity
import com.example.movie_application.model.Movie
import kotlinx.coroutines.launch

class MovieDetailsFragment : Fragment(R.layout.fragment_movie_details) {

    private lateinit var binding: FragmentMovieDetailsBinding
    private lateinit var db: MovieDatabase
    private var isMovieSaved = false
    private val castAdapter by lazy { CastAdapter() }
    private val similarMoviesAdapter by lazy { MovieAdapter() }
    private val reviewAdapter by lazy { ReviewAdapter() } // 👈 Review Adapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMovieDetailsBinding.bind(view)

        db = MovieDatabase.getDatabase(requireContext())

        setupRecyclerViews()

        val movie = arguments?.getParcelable<Movie>("movie")

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        movie?.let { currentMovie ->
            setupUI(currentMovie)
            checkIfSaved(currentMovie.id)

            setupTrailer(currentMovie.id)

            getCast(currentMovie.id)

            getReviews(currentMovie.id)

            getSimilarMovies(currentMovie.id)

            binding.btnWatchlist.setOnClickListener {
                if (isMovieSaved) {
                    deleteMovie(currentMovie.id)
                } else {
                    saveMovie(currentMovie)
                }
            }
        }

        similarMoviesAdapter.setOnItemClickListener { movie ->
            val bundle = Bundle().apply {
                putParcelable("movie", movie)
            }
            try {
                findNavController().navigate(R.id.action_movieDetailsFragment_self, bundle)
            } catch (e: Exception) {
                Log.e("Navigation", "Navigation action not found: ${e.message}")
            }
        }

        castAdapter.setOnItemClickListener { cast ->
            val bundle = Bundle().apply {
                putInt("personId", cast.id)
            }
            findNavController().navigate(R.id.action_movieDetailsFragment_to_actorDetailsFragment, bundle)
        }
    }

    private fun setupRecyclerViews() {
        binding.rvCast.apply {
            adapter = castAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvReviews.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvSimilarMovies.apply {
            adapter = similarMoviesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun getReviews(movieId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getMovieReviews(movieId)
                if (response.isSuccessful) {
                    val reviews = response.body()?.results?.take(5) ?: emptyList()

                    if (reviews.isNotEmpty()) {
                        reviewAdapter.submitList(reviews)
                        binding.tvReviewsLabel.visibility = View.VISIBLE
                        binding.rvReviews.visibility = View.VISIBLE
                    } else {
                        binding.tvReviewsLabel.visibility = View.GONE
                        binding.rvReviews.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e("Reviews", "Error loading reviews: ${e.message}")
            }
        }
    }

    private fun getCast(movieId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getMovieCredits(movieId)
                if (response.isSuccessful) {
                    val castList = response.body()?.cast?.take(10) ?: emptyList()
                    castAdapter.submitList(castList)
                }
            } catch (e: Exception) {
                Log.e("Cast", "Error loading cast: ${e.message}")
            }
        }
    }

    private fun getSimilarMovies(movieId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getSimilarMovies(movieId)
                if (response.isSuccessful) {
                    val movies = response.body()?.results ?: emptyList()
                    similarMoviesAdapter.differ.submitList(movies)

                    if (movies.isEmpty()) {
                        binding.tvSimilarLabel.visibility = View.GONE
                        binding.rvSimilarMovies.visibility = View.GONE
                    } else {
                        binding.tvSimilarLabel.visibility = View.VISIBLE
                        binding.rvSimilarMovies.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e("Similar", "Error loading similar movies: ${e.message}")
            }
        }
    }

    private fun setupTrailer(movieId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getMovieVideos(movieId)
                if (response.isSuccessful) {
                    val videos = response.body()?.results
                    val trailer = videos?.find { it.site == "YouTube" && it.type == "Trailer" }

                    if (trailer != null) {
                        Log.d("YouTubeCheck", "Found Trailer ID: ${trailer.key}")
                        binding.tvWatchTrailer.visibility = View.VISIBLE
                        binding.tvWatchTrailer.setOnClickListener {
                            openYoutubeVideo(trailer.key)
                        }
                    } else {
                        binding.tvWatchTrailer.visibility = View.GONE
                    }
                } else {
                    binding.tvWatchTrailer.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("DetailsFragment", "Error loading trailer: ${e.message}")
                binding.tvWatchTrailer.visibility = View.GONE
            }
        }
    }

    private fun openYoutubeVideo(videoId: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))

        try {
            requireContext().startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            requireContext().startActivity(webIntent)
        }
    }

    private fun setupUI(movie: Movie) {
        binding.apply {
            tvDetailTitle.text = movie.title
            tvOverview.text = movie.overview
            tvDetailRating.text = " ${String.format("%.1f", movie.rating)}"
            tvReleaseDate.text = movie.releaseDate
            ivBackdrop.load(IMAGE_BASE_URL + movie.posterPath) { crossfade(true) }
        }
    }

    private fun checkIfSaved(id: Int) {
        lifecycleScope.launch {
            isMovieSaved = db.getMovieDao().isMovieSaved(id)
            updateWatchlistIcon(isMovieSaved)
        }
    }

    private fun saveMovie(movie: Movie) {
        val entity = MovieEntity(
            id = movie.id,
            title = movie.title ?: "Unknown",
            posterPath = movie.posterPath,
            rating = movie.rating ?: 0.0,
            releaseDate = movie.releaseDate ?: "",
            overview = movie.overview ?: ""
        )

        lifecycleScope.launch {
            db.getMovieDao().insertMovie(entity)
            isMovieSaved = true
            updateWatchlistIcon(true)
            Toast.makeText(context, "Saved to Watchlist", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteMovie(id: Int) {
        lifecycleScope.launch {
            db.getMovieDao().deleteMovie(id)
            isMovieSaved = false
            updateWatchlistIcon(false)
            Toast.makeText(context, "Removed from Watchlist", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWatchlistIcon(saved: Boolean) {
        if (saved) {
            binding.btnWatchlist.setImageResource(R.drawable.ic_bookmark)
            binding.btnWatchlist.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_color))
        } else {
            binding.btnWatchlist.setImageResource(R.drawable.ic_bookmark)
            binding.btnWatchlist.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }
}