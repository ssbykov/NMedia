package ru.netology.nmedia.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.Constants.BASE_URL_AVATAR
import ru.netology.nmedia.Constants.BASE_URL_IMAGES
import ru.netology.nmedia.Constants.SDRF
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.formatCount
import java.text.SimpleDateFormat
import java.util.Locale


class PostsAdapter(
    private val setupClickListeners: SetupClickListeners,
) :
    ListAdapter<Post, PostViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, setupClickListeners)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem

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