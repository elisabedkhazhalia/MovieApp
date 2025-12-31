package com.example.movie_application.ui.theme.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movie_application.R
import com.example.movie_application.databinding.FragmentHomeBinding
import com.example.movie_application.adapters.MovieAdapter
import com.google.android.material.chip.Chip

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val movieAdapter = MovieAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupRecyclerView()

        // 1. კლიკის ლოგიკა (დეტალებზე გადასვლა)
        movieAdapter.setOnItemClickListener { movie ->
            val bundle = Bundle().apply {
                putParcelable("movie", movie)
            }
            findNavController().navigate(
                R.id.action_homeFragment_to_movieDetailsFragment,
                bundle
            )
        }

        // 2. მონაცემების მოსმენა (LiveData)
        viewModel.movies.observe(viewLifecycleOwner) { moviesList ->
            movieAdapter.differ.submitList(moviesList)
        }

        // 3. ჟანრების ფილტრაცია (ახალი კოდი) 👇
        setupChipFilters()
    }

    private fun setupChipFilters() {
        // თავიდან ჩავტვირთოთ პოპულარული
        viewModel.getPopularMovies()

        binding.chipGroupGenres.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)

            if (chip != null) {
                // ვიღებთ ჟანრის ID-ს XML-ის "tag" ატრიბუტიდან (მაგ: "28")
                val genreId = chip.tag?.toString()?.toIntOrNull()

                if (genreId != null) {
                    // თუ კონკრეტული ჟანრია არჩეული (Action, Comedy...)
                    viewModel.getMoviesByGenre(genreId)
                } else {
                    // თუ "Popular" არის არჩეული (მას tag არ დავუწერეთ)
                    viewModel.getPopularMovies()
                }
            } else {
                // თუ არაფერია მონიშნული, ისევ პოპულარული
                viewModel.getPopularMovies()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvMovies.apply {
            adapter = movieAdapter
            layoutManager = GridLayoutManager(activity, 2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}