package com.byagowi.persiancalendar.ui.converter

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.ColorInt
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import androidx.core.view.isVisible
import com.byagowi.persiancalendar.ui.utils.shareBinaryFile
import com.byagowi.persiancalendar.ui.utils.toPngByteArray
import com.byagowi.persiancalendar.utils.logException
import io.github.persiancalendar.qr.qr
import kotlin.math.min

class QrView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var qr: List<List<Boolean>> = emptyList()
    private var previousQr: List<List<Boolean>> = emptyList()
    private var roundness = 1f
    private var viewSize = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setContentColor(@ColorInt color: Int) {
        paint.color = color
    }

    override fun onDraw(canvas: Canvas) {
        // How much it should be similar to qr vs previousQr, 1 for qr, 0 for previousQr
        val transitionFactor = transitionAnimator.animatedFraction
        drawQr(canvas, viewSize, transitionFactor, qr, previousQr)
    }

    private val roundnessAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        it.interpolator = AccelerateDecelerateInterpolator()
        it.addUpdateListener { _ ->
            roundness = it.animatedFraction
            invalidate()
        }
    }

    init {
        setOnClickListener {
            if (roundness < .5f) roundnessAnimator.start() else roundnessAnimator.reverse()
        }

        // Show something in Android Studio preview
        if (Build.DEVICE == "layoutlib") {
            paint.color = Color.BLACK
            update("Sample Text")
        }
    }

    private val rect = RectF()
    private val path = Path()
    private fun drawQr(
        canvas: Canvas, size: Int, factor: Float,
        qr: List<List<Boolean>>, previousQr: List<List<Boolean>>
    ) {
        val cells = qr.size // cells in a row or a column
        val cellSize = size.toFloat() / (qr.size.takeIf { it != 0 } ?: return)
        (0..<cells).forEach { i ->
            (0..<cells).forEach { j ->
                if ((i > 6 || j > 6) && (cells - i > 7 || j > 6) && (i > 6 || cells - j > 7)) {
                    val current = qr[i][j]
                    val previous = previousQr.getOrNull(i)?.getOrNull(j) == true
                    when {
                        current && previous -> drawDot(canvas, i, j, cellSize, 1f)
                        current && !previous -> drawDot(canvas, i, j, cellSize, factor)
                        !current && previous -> drawDot(canvas, i, j, cellSize, 1 - factor)
                    }
                }
            }
        }
        path.rewind()
        rect.set(0f, 0f, cellSize * 7, cellSize * 7)
        val round = cellSize * roundness
        path.addRoundRect(rect, round * 2, round * 2, Path.Direction.CW)
        rect.set(cellSize, cellSize, cellSize * 6, cellSize * 6)
        path.addRoundRect(rect, round * 1.5f, round * 1.5f, Path.Direction.CCW)
        rect.set(cellSize * 2, cellSize * 2, cellSize * 5, cellSize * 5)
        path.addRoundRect(rect, round, round, Path.Direction.CW)
        canvas.drawPath(path, paint)
        canvas.withTranslation(0f, cellSize * (qr.size - 7)) { canvas.drawPath(path, paint) }
        canvas.withTranslation(cellSize * (qr.size - 7), 0f) { canvas.drawPath(path, paint) }
    }

    private fun drawDot(canvas: Canvas, i: Int, j: Int, cellSize: Float, factor: Float) {
        rect.set(i * cellSize, j * cellSize, (i + 1) * cellSize, (j + 1) * cellSize)
        val inset = (roundness - 1) / 4 + (1 - factor) * cellSize / 2
        rect.inset(inset, inset)
        val r = roundness * cellSize / 2 * factor
        canvas.drawRoundRect(rect, r, r, paint)
    }

    fun share(@ColorInt backgroundColor: Int) {
        val size = 1280f
        val bitmap = createBitmap(size.toInt(), size.toInt()).applyCanvas {
            drawColor(backgroundColor)
            withScale(1 - 64 / size, 1 - 64 / size, size / 2, size / 2) {
                drawQr(this, size.toInt(), 1f, qr, qr)
            }
        }
        context.shareBinaryFile(bitmap.toPngByteArray(), "result.png", "image/png")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        viewSize = min(
            MeasureSpec.getSize(widthMeasureSpec),
            resources.displayMetrics.let {
                if (it.heightPixels > it.widthPixels) it.widthPixels
                else it.heightPixels * 2 / 3
            }
        )
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(viewSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(viewSize, MeasureSpec.EXACTLY)
        )
    }

    private val transitionAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        it.interpolator = OvershootInterpolator()
        it.addUpdateListener { invalidate() }
    }

    fun update(text: String) {
        runCatching {
            previousQr = qr
            qr = qr(text)
            transitionAnimator.start()
            if (!isVisible) isVisible = true
            invalidate()
        }.onFailure(logException).onFailure {
            isVisible = false
        }
    }
}
