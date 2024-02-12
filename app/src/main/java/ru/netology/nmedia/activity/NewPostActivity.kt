package ru.netology.nmedia.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.databinding.ActivityNewPostBinding

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNewPostBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            content.requestFocus()
            content.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
            ok.setOnClickListener {
                val text = content.text.toString()
                if (content.text.isNotBlank()) {
                    setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_TEXT, text))
                } else setResult(RESULT_CANCELED)
                finish()
            }

        }
    }
}

object NewPostContract : ActivityResultContract<String?, String?>() {
    override fun createIntent(context: Context, input: String?): Intent {
        val intent = Intent(context, NewPostActivity::class.java)
        intent.putExtra(Intent.EXTRA_TEXT, input)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        intent?.getStringExtra(Intent.EXTRA_TEXT)

}