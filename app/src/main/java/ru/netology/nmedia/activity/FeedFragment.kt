package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.Constants.KEY_ATTACHMENT
import ru.netology.nmedia.Constants.KEY_CONTENT
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.adapter.PostsSetupClickListeners
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private lateinit var binding: FragmentFeedBinding

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    companion object {
        var Bundle.textArg: String? by StringArg
        var Bundle.urlArg: String? by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.auth_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.sign_in -> {
                        findNavController().navigate(R.id.loginFragment)
                        true
                    }

                    R.id.sign_up -> {
                        findNavController().navigate(R.id.registrationFragment)
                        true
                    }

                    R.id.logout -> {
                        val draftPrefs =
                            binding.root.context.getSharedPreferences(
                                "draft",
                                android.content.Context.MODE_PRIVATE
                            )
                        val content = draftPrefs.getString(KEY_CONTENT, "").toString()
                        val uri = draftPrefs.getString(KEY_ATTACHMENT, "").toString()

                        return if (content.isNotEmpty() || !uri.isNullOrBlank() && uri != "null") {
                            MaterialAlertDialogBuilder(binding.root.context)
                                .setTitle(getString(R.string.exit_confirmation_title))
                                .setMessage(getString(R.string.exit_confirmation_message))
                                .setNeutralButton(getString(R.string.exit)) { _, _ ->
                                    appAuth.clearAuth()
                                }
                                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                                    dialog.cancel()
                                }
                                .show()
                            true
                        } else {
                            appAuth.clearAuth()
                            true
                        }
                    }

                    else -> false
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                viewModel.isLogin.observe(viewLifecycleOwner) {
                    menu.setGroupVisible(R.id.authenticated, it)
                    menu.setGroupVisible(R.id.unauthenticated, !it)
                }
            }
        }, viewLifecycleOwner)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PostsAdapter(PostsSetupClickListeners(viewModel, this))
        binding.list.adapter = adapter


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.data.collectLatest {
                    adapter.submitData(it)
                }
            }
        }


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                adapter.loadStateFlow.collectLatest {
                    binding.swiper.isRefreshing = it.refresh is LoadState.Loading
                            || it.append is LoadState.Loading
                }
            }
        }

//            viewModel.newerCount.observe(viewLifecycleOwner) {
//                if (it != null && it > 0) {
//                    binding.newPosts.text = getString(R.string.new_posts, it.toString())
//                    binding.newPosts.visibility = View.VISIBLE
//                }
//
//            }

//            adapter.notifyDataSetChanged()

//            viewModel.dataState.observe(viewLifecycleOwner) { state ->
//                binding.progress.isVisible = state.loading
//                if (state.error) {
//                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
//                        .setAction(R.string.retry_loading) {
//                            viewModel.loadPosts()
//                        }
//                        .setAnchorView(binding.add)
//                        .show()
//                }
//            }

            binding.swiper.setOnRefreshListener {
                adapter.refresh()
                binding.swiper.isRefreshing = false
                binding.newPosts.visibility = View.GONE
            }

            binding.newPosts.setOnClickListener {
                viewModel.showAll()
                binding.newPosts.visibility = View.GONE
            }


            binding.add.setOnClickListener {
                viewModel.isLogin.observe(viewLifecycleOwner) { state ->
                    if (!state) {
                        findNavController().navigate(R.id.action_feedFragment_to_loginFragment)
                    } else {
                        viewModel.dropPhoto()
                        findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
                    }
                }
            }

        }
    }

