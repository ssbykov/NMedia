package ru.netology.nmedia.utils

import android.os.Bundle
import kotlin.math.floor
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

object StringArg : ReadWriteProperty<Bundle, String?> {

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: String?) {
        thisRef.putString(property.name, value)
    }

    override fun getValue(thisRef: Bundle, property: KProperty<*>): String? =
        thisRef.getString(property.name)
}