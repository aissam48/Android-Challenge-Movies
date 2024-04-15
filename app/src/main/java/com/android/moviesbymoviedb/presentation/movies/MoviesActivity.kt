package com.android.moviesbymoviedb.presentation.movies

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.android.moviesbymoviedb.R
import com.android.moviesbymoviedb.databinding.ActivityMoviesBinding
import com.android.moviesbymoviedb.presentation.viewmodel.sealed.EventUI
import com.android.moviesbymoviedb.domain.models.MovieModel
import com.android.moviesbymoviedb.domain.utils.collectLatestLifecycleFlow
import com.android.moviesbymoviedb.domain.utils.goForwardAnimation
import com.android.moviesbymoviedb.domain.utils.gone
import com.android.moviesbymoviedb.domain.utils.isInternetConnected
import com.android.moviesbymoviedb.domain.utils.visible
import com.android.moviesbymoviedb.presentation.favorites_movies.FavoritesActivity
import com.android.moviesbymoviedb.presentation.movie_details.MovieDetailsActivity
import com.android.moviesbymoviedb.presentation.viewmodel.MoviesViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoviesActivity : AppCompatActivity() {

    lateinit var binding: ActivityMoviesBinding
    private val viewModel by viewModels<MoviesViewModel>()
    private lateinit var moviesAdapter: MoviesAdapter
    private val REQUEST_CODE = 12

    private val TAG = "MoviesActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoviesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpAdapter()
        collectData()
        setUpClick()
        setUpPagination()
        setUpSearch()

        if (isInternetConnected()) {
            viewModel.fetchMovies("comedy")
        } else {
            viewModel.fetchMoviesFromLocalDatabase()
        }

    }

    private fun setUpSearch() {
        binding.etSearch.doOnTextChanged { text, start, before, count ->
            if (isInternetConnected()) {
                viewModel.listOfMovies.clear()
                viewModel.page = 1
                if (text.toString().isEmpty()) {
                    viewModel.fetchMovies("comedy")
                } else {
                    viewModel.fetchMovies(text.toString())
                }
            } else {
                moviesAdapter.submitList(viewModel.listOfMovies.filter {
                    it.name.lowercase().contains(text.toString().lowercase())
                })
            }
        }
    }

    private fun setUpPagination() {
        binding.rvMovies.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(0) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (isInternetConnected()) {
                        viewModel.page = viewModel.page + 1
                        val searchInput = binding.etSearch.text.toString()
                        if (searchInput.isEmpty()) {
                            viewModel.fetchMovies("comedy")
                        } else {
                            viewModel.fetchMovies(searchInput)
                        }
                    }
                }
            }
        })
    }

    private fun setUpClick() {

        binding.buttonFavorites.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
            goForwardAnimation()
        }

        binding.buttonSort.setOnClickListener {
            if (binding.llSearch.isVisible) {
                binding.llSearch.gone()
                binding.llSort.visible()
                binding.buttonSort.setImageResource(R.drawable.ic_search_gray)
            } else {
                binding.llSearch.visible()
                binding.llSort.gone()
                binding.buttonSort.setImageResource(R.drawable.ic_sort)
            }
        }

        binding.buttonSortByAlpha.setOnClickListener {
            binding.buttonSortByAlpha.background =
                ContextCompat.getDrawable(this, R.drawable.bg_button_selected)
            binding.buttonSortByDate.background =
                ContextCompat.getDrawable(this, R.drawable.bg_button)
            binding.buttonSortByDate.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.buttonSortByAlpha.setTextColor(ContextCompat.getColor(this, R.color.white))

            moviesAdapter.submitList(viewModel.listOfMovies.sortedBy { it.name })
        }

        binding.buttonSortByDate.setOnClickListener {
            binding.buttonSortByDate.background =
                ContextCompat.getDrawable(this, R.drawable.bg_button_selected)
            binding.buttonSortByAlpha.background =
                ContextCompat.getDrawable(this, R.drawable.bg_button)
            binding.buttonSortByAlpha.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.buttonSortByDate.setTextColor(ContextCompat.getColor(this, R.color.white))

            moviesAdapter.submitList(viewModel.listOfMovies.sortedBy { it.firstAirDate })
        }

    }

    private fun setUpAdapter() {
        moviesAdapter = MoviesAdapter {
            val intent = Intent(this, MovieDetailsActivity::class.java)
            intent.putExtra("movie", Gson().toJson(it))
            startActivityForResult(intent, REQUEST_CODE)
            goForwardAnimation()
        }
        binding.rvMovies.adapter = moviesAdapter
    }

    private fun collectData() {
        collectLatestLifecycleFlow(viewModel.sharedFlowFetchMovies) {
            when (it) {
                is EventUI.OnLoading -> {
                    updateLoading(it.isShowing)
                }

                is EventUI.OnSuccess -> {
                    updateIU(it.data)
                }

                is EventUI.OnError -> {
                    Snackbar.make(binding.root, it.message, 1500)
                        .setTextColor(ContextCompat.getColor(this, R.color.white))
                        .setBackgroundTint(ContextCompat.getColor(this, R.color.color4))
                        .show()
                }
            }
        }

        collectLatestLifecycleFlow(viewModel.sharedFlowUpdateItem) {
            moviesAdapter.notifyItemChanged(it)
        }

    }

    private fun updateIU(data: List<MovieModel>?) {
        if (data == null) {
            return
        }

        moviesAdapter.submitList(data)
        viewModel.insertMoviesToLocalDatabase(data)

    }

    private fun updateLoading(showing: Boolean) {
        if (showing) {
            binding.prLoading.visible()
        } else {
            binding.prLoading.gone()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val model = data.getStringExtra("movie")
            if (model != null) {
                viewModel.updateItem(model)
            }

        }
    }

}