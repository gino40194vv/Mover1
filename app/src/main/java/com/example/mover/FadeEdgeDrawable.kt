package com.example.mover

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.widget.ImageView

// Classe personalizzata per l'effetto di dissolvenza
class FadeEdgeDrawable(private val originalDrawable: Drawable) : Drawable() {
    private val paint = Paint()
    private var fadeWidth = 0.3f

    init {
        setBounds(originalDrawable.bounds)
        paint.isAntiAlias = true
    }

    override fun draw(canvas: Canvas) {
        originalDrawable.draw(canvas)

        val bounds = bounds
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()

        val fadeStart = width * (1 - fadeWidth)

        val gradient = LinearGradient(
            fadeStart, 0f,
            width, 0f,
            intArrayOf(Color.TRANSPARENT, Color.WHITE),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )

        // Crea un nuovo Paint per il gradiente
        val overlayPaint = Paint().apply {
            shader = gradient
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            isAntiAlias = true
        }

        canvas.drawRect(fadeStart, 0f, width, height, overlayPaint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    fun setFadeWidth(width: Float) {
        fadeWidth = width.coerceIn(0f, 1f)
        invalidateSelf()
    }
}

fun ImageView.applicaEffettoDissolvenza(larghezzaDissolvenza: Float = 0.3f) {
    drawable?.let { drawableOriginale ->
        // Preserva il ColorFilter originale se presente
        val originalColorFilter = drawableOriginale.colorFilter

        val fadeDrawable = FadeEdgeDrawable(drawableOriginale).apply {
            setFadeWidth(larghezzaDissolvenza)
            colorFilter = originalColorFilter
        }

        setImageDrawable(fadeDrawable)
    }
}