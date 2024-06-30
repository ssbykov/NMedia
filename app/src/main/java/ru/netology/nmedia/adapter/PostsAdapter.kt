package ru.netology.nmedia.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.Constants.BASE_URL_AVATAR
import ru.netology.nmedia.Constants.BASE_URL_IMAGES
import ru.netology.nmedia.Constants.SDRF
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.AdCardBinding
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.databinding.TimingSeparatorCardBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TimingSeparator
import ru.netology.nmedia.utils.formatCount
import java.text.SimpleDateFormat
import java.util.Locale


class PostsAdapter(
    private val setupClickListeners: SetupClickListeners,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Ad -> R.layout.ad_card
            is Post, null -> R.layout.post_card
            is TimingSeparator -> R.layout.timing_separator_card
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            R.layout.post_card -> {
                val binding =
                    PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, setupClickListeners)
            }

            R.layout.ad_card -> {
                val binding =
                    AdCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }

            R.layout.timing_separator_card -> {
                val binding =
                    TimingSeparatorCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TimingSeparatorViewHolder(binding)
            }

            else -> error("unknown view type $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as AdViewHolder).bind(item)
            is Post -> (holder as PostViewHolder).bind(item)
            is TimingSeparator -> (holder as TimingSeparatorViewHolder).bind(item)
            null -> Unit
        }
    }
}

object PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem.id == newItem.id && oldItem::class == newItem::class
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem) = oldItem == newItem

}

class AdViewHolder(
    private val binding: AdCardBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        Glide.with(binding.imageAd)
            .load("$BASE_URL_IMAGES${ad.image}")
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(30_000)
            .into(binding.imageAd)
    }
}

class TimingSeparatorViewHolder(
    private val binding: TimingSeparatorCardBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(timingSeparator: TimingSeparator) {
        binding.separator.text = timingSeparator.period
    }
}

class PostViewHolder(
    private val binding: PostCardBinding,
    private val setupClickListeners: SetupClickListeners,
) :
    RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("ResourceType")
    fun bind(post: Post) {
        with(binding) {
            //заполнение значений элементов поста
            author.text = post.author
            published.text = SimpleDateFormat(SDRF, Locale("ru"))
                .format(post.published * 1000)
            content.text = post.content
            like.isChecked = post.likedByMe
            like.text = formatCount(post.likes)
            shear.text = formatCount(post.shares)
            views.text = formatCount(post.views)

            Glide.with(avatar)
                .load("$BASE_URL_AVATAR${post.authorAvatar}")
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_face_48)
                .timeout(30_000)
                .circleCrop()
                .into(avatar)

            if (post.attachment != null) {
                Glide.with(attachment)
                    .load("$BASE_URL_IMAGES${post.attachment.url}")
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(30_000)
                    .into(attachment)
                attachment.visibility = View.VISIBLE
            } else attachment.visibility = View.GONE

            // установка слушателей
            like.setOnClickListener {
                setupClickListeners.onLikeListener(post)
                like.isChecked = post.likedByMe
            }
            shear.setOnClickListener {
                setupClickListeners.onShareListener(post)
            }
            if (post.video != null) {
                play.setOnClickListener {
                    setupClickListeners.onPlayListener(post)
                }
                preview.visibility = View.VISIBLE
                play.visibility = View.VISIBLE
            } else {
                preview.visibility = View.GONE
                play.visibility = View.GONE
            }
            content.setOnClickListener {
                setupClickListeners.onPostListener(post)
            }

            attachment.setOnClickListener {
                setupClickListeners.onImageListener(post.attachment?.url)
            }
            // инициализация меню
            menu.isVisible = post.ownedByMy
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                setupClickListeners.onRemoveListener(post)
                                true
                            }

                            R.id.edit -> {
                                setupClickListeners.onEditListener(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }

    }
}