package ru.netology.nmedia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.formatCount

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = initPost()
        initViews(binding, post)

    }

    private fun setupClickListeners(binding: ActivityMainBinding, post: Post) {
        with(binding) {
            like.setOnClickListener {
                if (post.likedByMe) post.likes-- else post.likes++
                if (post.likes < 0) post.likes = 0
                post.likedByMe = !post.likedByMe
                like.setImageResource(
                    if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24
                )

                likeCount.text = formatCount(post.likes)
            }
            shear.setOnClickListener {
                post.shares++
                shareCount.text = formatCount(post.shares)
            }
        }
    }

    private fun initViews(binding: ActivityMainBinding, post: Post) {
        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likeCount.text = formatCount(post.likes)
            shareCount.text = formatCount(post.shares)
            viewsCount.text = formatCount(post.views)
            if (post.likedByMe) like.setImageResource(R.drawable.ic_liked_24)
        }
        setupClickListeners(binding, post)
    }
}


fun initPost(): Post {
    return Post(
        id = 1,
        author = "Нетология. Университет интернет-профессий будущего",
        content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
        published = "21 мая в 18:36",
        likedByMe = true,
        likes = 0,
        shares = 1_199_999,
        views = 1001
    )
}