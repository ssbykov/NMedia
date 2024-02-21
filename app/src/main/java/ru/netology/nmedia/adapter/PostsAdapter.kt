package ru.netology.nmedia.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.formatCount

interface SetupClickListeners {
    fun onLikeListener(post: Post)
    fun onShareListener(post: Post)
    fun onRemoveListener(post: Post)
    fun onEditListener(post: Post)
}

class PostsAdapter(
    private val setupClickListeners: SetupClickListeners,
    private val intent: Intent
) :
    ListAdapter<Post, PostViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, setupClickListeners, intent)
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
    private val intent: Intent
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        with(binding) {
            author.text = post.author
            published.text = post.published
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
                    intent.data = Uri.parse(post.video)
                    startActivity(play.context, intent, null)
                }
                preview.visibility = View.VISIBLE
                play.visibility = View.VISIBLE
            } else {
                preview.visibility = View.GONE
                play.visibility = View.GONE
            }
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