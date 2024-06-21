package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.Constants.BASE_URL_IMAGES
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.AdCardBinding
import ru.netology.nmedia.databinding.ItemLoadingBinding
import ru.netology.nmedia.dto.Ad

class PostLoadingStateAdapter(private val retryListener: () -> Unit) :
    LoadStateAdapter<PostLoading>() {
    override fun onBindViewHolder(holder: PostLoading, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): PostLoading {
        val binding =
            ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostLoading(binding, retryListener)
    }
}

class PostLoading(
    private val binding: ItemLoadingBinding,
    private val retryListener: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(loadState: LoadState) {
        binding.apply {
            progressLoading.isVisible = loadState is LoadState.Loading
            retryLoading.isVisible = loadState is LoadState.Error
            retryLoading.setOnClickListener {
                retryListener()
            }

        }
    }
}