package com.byagowi.persiancalendar.ui.converter

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withScale
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.shareBinaryFile
import com.byagowi.persiancalendar.ui.utils.toByteArray
import com.byagowi.persiancalendar.utils.logException
import kotlin.math.min

class QrView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var qr: List<List<Boolean>> = emptyList()
    private var roundness = 0f
    private var viewSize = 0
    private val paint = Paint().also {
        it.color = context.resolveColor(android.R.attr.textColorPrimary)
    }

    override fun onDraw(canvas: Canvas) = drawQr(canvas, viewSize)

    private var animator: ValueAnimator? = null

    init {
        setOnClickListener {
            animator?.cancel()
            ValueAnimator.ofFloat(roundness, if (roundness > .5f) 0f else 1f).also {
                animator = it
                it.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
                it.interpolator = AccelerateDecelerateInterpolator()
                it.addUpdateListener { _ ->
                    roundness = it.animatedValue as? Float ?: return@addUpdateListener
                    invalidate()
                }
            }.start()
        }
    }

    private val rect = RectF()
    private fun drawQr(canvas: Canvas, size: Int) {
        val cellSize = size.toFloat() / (qr.size.takeIf { it != 0 } ?: return)
        qr.forEachIndexed { i, row ->
            row.forEachIndexed { j, v ->
                if (v) {
                    rect.set(
                        i * cellSize, j * cellSize,
                        (i + 1) * cellSize, (j + 1) * cellSize
                    )
                    rect.inset(-.25f * (1 - roundness), -.25f * (1 - roundness))
                    canvas.drawRoundRect(
                        rect,
                        roundness * cellSize / 2,
                        roundness * cellSize / 2,
                        paint
                    )
                }
            }
        }
    }

    fun share(activity: FragmentActivity?) {
        val size = 1280f
        val bitmap = createBitmap(size.toInt(), size.toInt())
        Canvas(bitmap).apply {
            drawColor(context.resolveColor(com.google.android.material.R.attr.colorSurface))
            withScale(1 - 64 / size, 1 - 64 / size, size / 2, size / 2) {
                drawQr(this, bitmap.width)
            }
        }
        activity?.shareBinaryFile(bitmap.toByteArray(), "result.png", "image/png")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        viewSize = min(
            MeasureSpec.getSize(widthMeasureSpec),
            context.resources.displayMetrics.let {
                if (it.heightPixels > it.widthPixels) it.widthPixels
                else it.heightPixels * 2 / 3
            }
        )
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(viewSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(viewSize, MeasureSpec.EXACTLY)
        )
    }

    fun update(text: String) {
        runCatching {
            qr = qr(text)
            if (!isVisible) isVisible = true
            invalidate()
        }.onFailure(logException).onFailure {
            isVisible = false
        }
    }
}
