package com.example.mover

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.HorizontalScrollView
import kotlin.math.abs

class CustomHorizontalScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : HorizontalScrollView(context, attrs, defStyle) {

    private var xDistance = 0f
    private var yDistance = 0f
    private var lastX = 0f
    private var lastY = 0f

    // La soglia minima per considerare il gesto uno scroll
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    // Fattore moltiplicativo per rendere l'intercettazione del gesto orizzontale meno sensibile
    private val horizontalSensitivityFactor = 3

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                xDistance = 0f
                yDistance = 0f
                lastX = ev.x
                lastY = ev.y
                super.onInterceptTouchEvent(ev)
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                val curX = ev.x
                val curY = ev.y
                xDistance += abs(curX - lastX)
                yDistance += abs(curY - lastY)
                lastX = curX
                lastY = curY

                // Intercetta solo se il movimento orizzontale è molto significativo:
                if (xDistance > yDistance && xDistance > touchSlop * horizontalSensitivityFactor) {
                    return true
                } else {
                    return false
                }
            }
            else -> return super.onInterceptTouchEvent(ev)
        }
    }
}
