package me.digitalnapotvrda.barcode

import android.graphics.*
import androidx.core.content.ContextCompat
import me.digitalnapotvrda.R
import me.digitalnapotvrda.camera.GraphicOverlay


internal class BarcodeGraphicBase(overlay: GraphicOverlay) : GraphicOverlay.Graphic(overlay) {

    private val scrimPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.scanner_background)
    }

    var boxRect: RectF? = null

    init {
        val overlayWidth = overlay.width.toFloat()
        val overlayHeight = overlay.height.toFloat()
        val boxSize = overlayWidth * 0.7
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        boxRect = RectF(
            (cx - boxSize / 2).toFloat(),
            (cy - boxSize / 2).toFloat(), (cx + boxSize / 2).toFloat(), (cy + boxSize / 2).toFloat()
        )
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), scrimPaint)
    }

}