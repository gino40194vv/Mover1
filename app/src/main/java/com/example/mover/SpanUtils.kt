package com.example.mover

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan

object SpanUtils {

    fun createColoredString(
        current: String,
        currentColor: Int,
        slashColor: Int,
        goal: String,
        goalColor: Int
    ): SpannableStringBuilder {

        val spannable = SpannableStringBuilder()

        spannable.append(current)
        spannable.setSpan(
            ForegroundColorSpan(currentColor),
            spannable.length - current.length,
            spannable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val slash = " / "
        spannable.append(slash)
        spannable.setSpan(
            ForegroundColorSpan(slashColor),
            spannable.length - slash.length,
            spannable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.append(goal)
        spannable.setSpan(
            ForegroundColorSpan(goalColor),
            spannable.length - goal.length,
            spannable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }
}
