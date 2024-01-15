package ru.netology.nmedia.utils

import kotlin.math.floor

fun formatCount(count: Int): String {
    return when (count) {
        in (0..999) -> count.toString()
        in (1000..1099) -> "1K"
        in (1100..9999) -> "${(floor(count / 100.0) / 10.0)}K"
        in (10_000..999_999) -> "${floor(count / 1000.0).toInt()}K"
        in (1_000_000..1_099_999) -> "1M"
        in (1_100_000..Int.MAX_VALUE) -> "${(floor(count / 100_000.0) / 10.0)}M"
        else -> "0"
    }
}