package com.android.moviesbymoviedb.presentation.favorites_movies

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.android.moviesbymoviedb.R
import com.android.moviesbymoviedb.databinding.ActivityFavoritesBinding
import com.android.moviesbymoviedb.presentation.viewmodel.sealed.EventUI
import com.android.moviesbymoviedb.domain.models.MovieModel
import com.android.moviesbymoviedb.presentation.movie_details.MovieDetailsActivity
import com.android.moviesbymoviedb.domain.utils.collectLatestLifecycleFlow
import com.android.moviesbymoviedb.domain.utils.goBackAnimation
import com.android.moviesbymoviedb.domain.utils.goForwardAnimation
import com.android.moviesbymoviedb.presentation.viewmodel.MoviesViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding

    private val viewModel by viewModels<MoviesViewModel>()
    private lateinit var favoritesAdapter: FavoritesAdapter

    private val REQUEST_CODE = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpAdapter()
        collectData()
        setOnClick()

        viewModel.fetchMoviesFromLocalDatabase()
    }

    private fun setOnClick() {
        binding.ivBack.setOnClickListener {
            finish()
            goBackAnimation()
        }
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

        collectLatestLifecycleFlow(viewModel.sharedFlowRemoveItem) {
            favoritesAdapter.notifyItemRemoved(it)
        }
    }

    private fun updateIU(data: List<MovieModel>?) {
        if (data==null){
            return
        }
        viewModel.favoriteMovies.clear()
        viewModel.favoriteMovies.addAll(data.filter { it.isFavorite })
        favoritesAdapter.submitList(viewModel.favoriteMovies)
    }

    private fun updateLoading(showing: Boolean) {

    }

    private fun setUpAdapter() {
        favoritesAdapter = FavoritesAdapter{ val intent = Intent(this, MovieDetailsActivity::class.java)
            intent.putExtra("movie", Gson().toJson(it))
            startActivityForResult(intent, REQUEST_CODE)
            goForwardAnimation()
        }
        binding.rvMovies.adapter = favoritesAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val model = data.getStringExtra("movie")
            if (model != null) {
                viewModel.removeFromFavorite(model)
            }

        }
    }

}