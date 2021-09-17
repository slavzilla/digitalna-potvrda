package me.digitalnapotvrda.barcode

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import me.digitalnapotvrda.R

class BarcodeGraphicRectangle(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val eraserPaint: Paint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
    }

    private val linePaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.action)
        isAntiAlias = true
        strokeWidth = 2 * context.resources.displayMetrics.density
        style = Paint.Style.STROKE
    }

    private val linePath = Path()

    private val lineOffset = 10 * context.resources.displayMetrics.density
    private val lineLength = 20 * context.resources.displayMetrics.density

    private val boxCornerRadius: Float =
        5 * context.resources.displayMetrics.density


    var boxRect: RectF? = null

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        boxRect = RectF(
            lineOffset,
            lineOffset,
            width - lineOffset,
            height - lineOffset
        )
        canvas.drawRoundRect(boxRect!!, boxCornerRadius, boxCornerRadius, eraserPaint)

        drawCornersInPath(width.toFloat(), height.toFloat())
        canvas.drawPath(linePath, linePaint)
    }

    private fun drawCornersInPath(width: Float, height: Float) {
        //top left
        linePath.moveTo(0f, lineLength)
        linePath.lineTo(0f, 0f)
        linePath.lineTo(lineLength, 0f)

        //top right
        linePath.moveTo(width - lineLength, 0f)
        linePath.lineTo(width, 0f)
        linePath.lineTo(width, lineLength)

        //bottom right
        linePath.moveTo(width, height - lineLength)
        linePath.lineTo(width, height)
        linePath.lineTo(width - lineLength, height)

        //bottom right
        linePath.moveTo(width, height - lineLength)
        linePath.lineTo(width, height)
        linePath.lineTo(width - lineLength, height)

        //bottom left
        linePath.moveTo(lineLength, height)
        linePath.lineTo(0f, height)
        linePath.lineTo(0f, height - lineLength)
    }
}