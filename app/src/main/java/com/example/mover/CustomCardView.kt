package com.example.mover

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

class CustomCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val defaultBottomRadius = dpToPx(8f)
    private val screenTopRadius = dpToPx(20f)

    init {
        updateCorners()
    }

    private fun updateCorners() {
        shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, screenTopRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, screenTopRadius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, defaultBottomRadius)
            .setBottomRightCorner(CornerFamily.ROUNDED, defaultBottomRadius)
            .build()
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }
}