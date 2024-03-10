package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.netology.nmedia.adapter.PostSetupClickListeners
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.adapter.PostsSetupClickListeners.Companion.textPostID
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class PostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentPostBinding.inflate(inflater, container, false)
        val postId: Long = (arguments?.textPostID ?: return binding.root).toLong()

        val startForResult = this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Обработка результата
                val data: Intent? = result.data
                // Используйте данные из Intent
            }
        }
//        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult(), callback)

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            posts.find { it.id == postId }?.let {
                PostViewHolder(
                    binding.postCard,
                    PostSetupClickListeners(viewModel, this, startForResult)
                ).bind(it)
            }
        }
        return binding.root
    }
}