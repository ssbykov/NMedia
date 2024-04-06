package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
    fun bind(post: Post) {
        with(binding) {
            author.text = post.author
            published.text = SimpleDateFormat("dd MMMM в H:mm", Locale("ru"))
                .format(post.published * 1000)
            content.text = post.content
            like.isChecked = post.likedByMe
            like.text = formatCount(post.likes)
            shear.text = formatCount(post.shares)
            views.text = formatCount(post.views)
            like.setOnClickListener {
                setupClickListeners.onLikeListener(post)
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
            // инициализация меню
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