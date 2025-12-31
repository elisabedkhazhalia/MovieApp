package com.example.movie_application.ui.watchlist

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_application.R
import com.example.movie_application.adapters.MovieAdapter
import com.example.movie_application.databinding.FragmentWatchlistBinding
import com.example.movie_application.db.MovieDatabase
import com.example.movie_application.db.MovieEntity
import com.example.movie_application.model.Movie
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class WatchlistFragment : Fragment(R.layout.fragment_watchlist) {

    private lateinit var binding: FragmentWatchlistBinding
    private val movieAdapter = MovieAdapter()

    // Icon and Background for Swipe
    private lateinit var deleteIcon: Drawable
    private val swipeBackground: ColorDrawable = ColorDrawable(Color.parseColor("#FF0000")) // Red Color

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWatchlistBinding.bind(view)

        // Initialize delete icon
        // Make sure you have created ic_delete in drawable folder!
        deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)!!

        setupRecyclerView()

        movieAdapter.setOnItemClickListener { movie ->
            val bundle = Bundle().apply { putParcelable("movie", movie) }
            findNavController().navigate(R.id.action_watchlistFragment_to_movieDetailsFragment, bundle)
        }

        getWatchlist()
    }

    private fun setupRecyclerView() {
        binding.rvWatchlist.apply {
            adapter = movieAdapter
            layoutManager = GridLayoutManager(activity, 2)
        }

        // SWIPE LOGIC 🎨
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT // Swipe Left only
        ) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val movie = movieAdapter.differ.currentList[position]
                deleteMovie(movie)
            }

            // Draw Red Background and Icon
            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

                if (dX < 0) { // Swiping Left
                    // 1. Red Background
                    swipeBackground.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    swipeBackground.draw(c)

                    // 2. Delete Icon
                    val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                    val iconBottom = iconTop + deleteIcon.intrinsicHeight

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rvWatchlist)
    }

    private fun deleteMovie(movie: Movie) {
        val db = MovieDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            db.getMovieDao().deleteMovie(movie.id)
            getWatchlist() // Refresh list

            // UNDO Logic
            Snackbar.make(binding.root, "${movie.title} removed", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    lifecycleScope.launch {
                        // Re-insert movie
                        val entity = MovieEntity(
                            id = movie.id,
                            title = movie.title ?: "Unknown",
                            posterPath = movie.posterPath,
                            rating = movie.rating ?: 0.0,
                            releaseDate = movie.releaseDate ?: "",
                            overview = movie.overview ?: ""
                        )
                        db.getMovieDao().insertMovie(entity)
                        getWatchlist()
                    }
                }
                .show()
        }
    }

    private fun getWatchlist() {
        val db = MovieDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val savedMovies = db.getMovieDao().getAllMovies()

            // FIXED: Removed 'backdropPath' and 'voteCount' because they don't exist in your Movie model
            val movieList = savedMovies.map { entity ->
                Movie(
                    id = entity.id,
                    title = entity.title,
                    posterPath = entity.posterPath,
                    overview = entity.overview,
                    releaseDate = entity.releaseDate,
                    rating = entity.rating
                )
            }
            movieAdapter.differ.submitList(movieList)
            binding.tvEmptyState.visibility = if (movieList.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        getWatchlist()
    }
}