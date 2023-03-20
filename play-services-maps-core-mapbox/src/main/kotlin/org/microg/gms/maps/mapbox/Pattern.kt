package org.microg.gms.maps.mapbox

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.PatternItem

fun PatternItem.getName(): String = when (this) {
    is Dash -> "dash${this.length}"
    is Gap -> "gap${this.length}"
    is Dot -> "dot"
    else -> this.javaClass.name
}

/**
 * Name of pattern, to identify it after it is added to map
 */
fun List<PatternItem>.getName(color: Int, strokeWidth: Float, skew: Float = 1f) = joinToString("-") {
    it.getName()
} + "-${color}-width${strokeWidth}-skew${skew}"

/**
 * Gets width that a bitmap for this pattern item would have if the pattern's bitmap
 * were to be drawn with respect to aspect ratio onto a canvas with height 1.
 */
fun PatternItem.getWidth(strokeWidth: Float, skew: Float): Float = when (this) {
    is Dash -> this.length
    is Gap -> this.length
    is Dot -> strokeWidth * skew
    else -> 1f
}

/**
 * Gets width that a bitmap for this pattern would have if it were to be drawn
 * with respect to aspect ratio onto a canvas with height 1.
 */
fun List<PatternItem>.getWidth(strokeWidth: Float, skew: Float) = map { it.getWidth(strokeWidth, skew) }.sum()

fun List<PatternItem>.makeBitmap(color: Int, strokeWidth: Float, skew: Float = 1f): Bitmap = makeBitmap(Paint().apply {
    setColor(color)
    style = Paint.Style.FILL
}, strokeWidth, skew)


fun List<PatternItem>.makeBitmap(paint: Paint, strokeWidth: Float, skew: Float): Bitmap {

    // Pattern aspect ratio is not respected by renderer
    val width = getWidth(strokeWidth, skew).toInt()
    val height = (strokeWidth * skew).toInt() // avoids squished image bugs

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    var drawCursor = 0f
    for (item in this) {
        when (item) {
            is Dash -> canvas.drawRect(
                drawCursor,
                0f,
                drawCursor + item.getWidth(strokeWidth, skew),
                strokeWidth * skew,
                paint
            )

            // is Gap -> do nothing, only move cursor

            is Dot -> canvas.drawOval(
                drawCursor,
                0f,
                drawCursor + item.getWidth(strokeWidth, skew),
                strokeWidth * skew,
                paint
            )
        }

        drawCursor += item.getWidth(strokeWidth, skew)
    }

    return bitmap
}