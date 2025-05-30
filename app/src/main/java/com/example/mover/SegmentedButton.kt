package com.example.mover

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

class SegmentedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {

    private var position: Int = POSITION_CENTER
    private var isButtonSelected = false
    private val defaultRadius = dpToPx(8f)
    private val screenRadius = dpToPx(20f)
    private var customTopLeftRadius: Float? = null
    private var customTopRightRadius: Float? = null
    private var customBottomLeftRadius: Float? = null
    private var customBottomRightRadius: Float? = null
    private var isDynamicTheme = false

    companion object {
        const val POSITION_START = 0
        const val POSITION_CENTER = 1
        const val POSITION_END = 2
    }

    init {
        // Controlla se il tema dinamico è attivo
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        isDynamicTheme = prefs.getInt("app_color", 0) == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        if (isDynamicTheme) {
            // Usa colori dal tema dinamico
            setupDynamicThemeColors()
        } else {
            // Usa colori statici predefiniti
            setBackgroundColor(ContextCompat.getColor(context, R.color.background_trofei))
            strokeWidth = dpToPx(1f).toInt()
            strokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.material_blue_giorni))
            setTextColor(ContextCompat.getColor(context, R.color.material_blue))
        }

        insetTop = 0
        insetBottom = 0
        minimumHeight = dpToPx(80f).toInt()
        gravity = Gravity.CENTER
        setPadding(dpToPx(16f).toInt(), dpToPx(12f).toInt(), dpToPx(16f).toInt(), dpToPx(12f).toInt())
    }

    private fun setupDynamicThemeColors() {
        // Ottieni i colori dal tema Material 3
        val typedValue = TypedValue()

        // Per il colore del bordo, usa colorPrimary
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
        val primaryColor = typedValue.data

        // Per il colore del testo, usa colorOnSurface
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        val onSurfaceColor = typedValue.data

        // Per lo sfondo, usa colorSurfaceVariant
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue, true)
        val surfaceVariantColor = typedValue.data

        // Applica i colori
        setBackgroundColor(surfaceVariantColor)
        strokeWidth = dpToPx(4f).toInt()
        strokeColor = ColorStateList.valueOf(primaryColor)
        setTextColor(onSurfaceColor)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Aggiunge i margini dopo che la view è stata attaccata alla window
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            (layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = dpToPx(3f).toInt()
                marginEnd = dpToPx(3f).toInt()
            }
            requestLayout()
        }
    }

    fun setCustomCornerRadii(
        topLeft: Float? = null,
        topRight: Float? = null,
        bottomLeft: Float? = null,
        bottomRight: Float? = null
    ) {
        customTopLeftRadius = topLeft?.let { dpToPx(it) }
        customTopRightRadius = topRight?.let { dpToPx(it) }
        customBottomLeftRadius = bottomLeft?.let { dpToPx(it) }
        customBottomRightRadius = bottomRight?.let { dpToPx(it) }
        updateCorners()
    }

    fun setPosition(position: Int) {
        this.position = position
        updateCorners()
    }

    private fun updateCorners() {
        val screenCornerRadius = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.resources.displayMetrics.density * 20
        } else {
            screenRadius
        }

        shapeAppearanceModel = when (position) {
            POSITION_START -> ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, customTopLeftRadius ?: screenCornerRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, customTopRightRadius ?: 0f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, customBottomLeftRadius ?: screenCornerRadius)
                .setBottomRightCorner(CornerFamily.ROUNDED, customBottomRightRadius ?: 0f)
                .build()

            POSITION_END -> ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, customTopLeftRadius ?: 0f)
                .setTopRightCorner(CornerFamily.ROUNDED, customTopRightRadius ?: screenCornerRadius)
                .setBottomLeftCorner(CornerFamily.ROUNDED, customBottomLeftRadius ?: 0f)
                .setBottomRightCorner(CornerFamily.ROUNDED, customBottomRightRadius ?: screenCornerRadius)
                .build()

            else -> ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, customTopLeftRadius ?: defaultRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, customTopRightRadius ?: defaultRadius)
                .setBottomLeftCorner(CornerFamily.ROUNDED, customBottomLeftRadius ?: defaultRadius)
                .setBottomRightCorner(CornerFamily.ROUNDED, customBottomRightRadius ?: defaultRadius)
                .build()
        }
    }

    override fun setSelected(selected: Boolean) {
        isButtonSelected = selected

        if (isDynamicTheme) {
            // Usa colori dal tema dinamico per lo stato selezionato/deselezionato
            val typedValue = TypedValue()

            if (selected) {
                // Per bottone selezionato, usa colorPrimary
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
                val primaryColor = typedValue.data

                // Per il testo del bottone selezionato, usa colorOnPrimary
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
                val onPrimaryColor = typedValue.data

                setBackgroundColor(primaryColor)
                setTextColor(onPrimaryColor)
                strokeWidth = dpToPx(0f).toInt() // Nessun bordo quando selezionato
            } else {
                // Per bottone non selezionato, usa colorSurfaceVariant
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue, true)
                val surfaceVariantColor = typedValue.data

                // Per il testo del bottone non selezionato, usa colorOnSurface
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
                val onSurfaceColor = typedValue.data

                // Per il bordo, usa colorPrimary
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
                val primaryColor = typedValue.data

                setBackgroundColor(surfaceVariantColor)
                setTextColor(onSurfaceColor)
                strokeWidth = dpToPx(1f).toInt()
                strokeColor = ColorStateList.valueOf(primaryColor)
            }
        } else {
            // Comportamento originale con colori statici
            if (selected) {
                setBackgroundColor(ContextCompat.getColor(context, R.color.background_trofei))
                setTextColor(ContextCompat.getColor(context, R.color.material_blue))
                strokeWidth = dpToPx(3.5f).toInt()
            } else {
                setBackgroundColor(ContextCompat.getColor(context, R.color.background_trofei))
                setTextColor(ContextCompat.getColor(context, R.color.material_blue))
                strokeWidth = dpToPx(0f).toInt()
            }
        }

        super.setSelected(selected)
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }
}