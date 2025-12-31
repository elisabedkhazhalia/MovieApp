package com.example.movie_application.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.movie_application.R // 👈 ეს დაამატე (რესურსებისთვის)
import com.example.movie_application.api.Constants
import com.example.movie_application.databinding.ItemMovieBinding
import com.example.movie_application.model.Movie

class MovieAdapter : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    inner class MovieViewHolder(val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = differ.currentList[position]

        holder.binding.apply {

            val imageLink = if (!movie.posterPath.isNullOrEmpty()) {
                Constants.IMAGE_BASE_URL + movie.posterPath
            } else {
                "" // ან null
            }

            ivMoviePoster.load(imageLink) {
                crossfade(true)
                placeholder(R.drawable.bg_gradient_shadow)
                error(R.drawable.bg_gradient_shadow)
            }

            val ratingValue = movie.rating ?: 0.0
            tvRating.text = String.format("%.1f", ratingValue)

            root.setOnClickListener {
                onItemClickListener?.let { it(movie) }
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClickListener: ((Movie) -> Unit)? = null
    fun setOnItemClickListener(listener: (Movie) -> Unit) {
        onItemClickListener = listener
    }
}