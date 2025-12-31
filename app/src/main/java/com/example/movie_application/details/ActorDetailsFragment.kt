package com.example.movie_application.ui.details

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.movie_application.R
import com.example.movie_application.adapters.MovieAdapter
import com.example.movie_application.api.Constants.IMAGE_BASE_URL
import com.example.movie_application.api.RetrofitInstance
import com.example.movie_application.databinding.FragmentActorDetailsBinding
import kotlinx.coroutines.launch

class ActorDetailsFragment : Fragment(R.layout.fragment_actor_details) {

    private lateinit var binding: FragmentActorDetailsBinding

    private val moviesAdapter by lazy { MovieAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentActorDetailsBinding.bind(view)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.rvActorMovies.apply {
            adapter = moviesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        moviesAdapter.setOnItemClickListener { movie ->
            val bundle = Bundle().apply {
                putParcelable("movie", movie)
            }
            try {
                findNavController().navigate(R.id.action_actorDetailsFragment_to_movieDetailsFragment, bundle)
            } catch (e: Exception) {
                Toast.makeText(context, "Navigation Error", Toast.LENGTH_SHORT).show()
            }
        }

        val personId = arguments?.getInt("personId") ?: return

        loadData(personId)
    }

    private fun loadData(id: Int) {
        lifecycleScope.launch {
            try {
                val personResponse = RetrofitInstance.api.getPersonDetails(id)
                if (personResponse.isSuccessful) {
                    val person = personResponse.body()
                    person?.let {
                        binding.apply {
                            tvActorName.text = it.name
                            tvBiography.text = if (it.biography.isNotEmpty()) it.biography else "No biography available."

                            val birthText = listOfNotNull(it.birthday, it.placeOfBirth).joinToString(", ")
                            tvBirthInfo.text = birthText

                            ivActorProfile.load(IMAGE_BASE_URL + it.profilePath) {
                                crossfade(true)
                                transformations(RoundedCornersTransformation(16f))
                                error(R.drawable.bg_gradient_shadow)
                            }
                        }
                    }
                }

                val moviesResponse = RetrofitInstance.api.getPersonMovieCredits(id)
                if (moviesResponse.isSuccessful) {
                    val movies = moviesResponse.body()?.cast ?: emptyList()
                    val validMovies = movies.filter { !it.posterPath.isNullOrEmpty() }

                    moviesAdapter.differ.submitList(validMovies)

                    if (validMovies.isEmpty()) {
                        binding.tvKnownForLabel.visibility = View.GONE
                        binding.rvActorMovies.visibility = View.GONE
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}