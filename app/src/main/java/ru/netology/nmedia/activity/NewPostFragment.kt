package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding

class NewPostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)
        with(binding) {
            content.requestFocus()
            val intent = Intent()
            content.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
            ok.setOnClickListener {
                val text = content.text.toString()
                if (content.text.isNotBlank()) {
                    activity?.setResult(
                        Activity.RESULT_OK,
                        Intent().putExtra(Intent.EXTRA_TEXT, text)
                    )
                } else activity?.setResult(Activity.RESULT_CANCELED)
                findNavController().navigateUp()
            }
        }
        return binding.root
    }

}

object NewPostContract : ActivityResultContract<String?, String?>() {
    override fun createIntent(context: Context, input: String?): Intent {
        val intent = Intent(context, NewPostFragment::class.java)
        intent.putExtra(Intent.EXTRA_TEXT, input)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        intent?.getStringExtra(Intent.EXTRA_TEXT)

}