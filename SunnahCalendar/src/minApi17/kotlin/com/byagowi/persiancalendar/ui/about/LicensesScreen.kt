package com.byagowi.persiancalendar.ui.about

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.style.ReplacementSpan
import android.text.util.Linkify
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.os.postDelayed
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.text.scale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.LicensesScreenBinding
import com.byagowi.persiancalendar.generated.EventType
import com.byagowi.persiancalendar.generated.gregorianEvents
import com.byagowi.persiancalendar.generated.irregularRecurringEvents
import com.byagowi.persiancalendar.generated.islamicEvents
import com.byagowi.persiancalendar.generated.nepaliEvents
import com.byagowi.persiancalendar.generated.persianEvents
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.ui.utils.sp
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.google.android.material.sidesheet.SideSheetBehavior
import com.google.android.material.sidesheet.SideSheetCallback
import com.google.android.material.transition.MaterialFadeThrough
import kotlin.math.roundToInt

class LicensesScreen : Fragment(R.layout.licenses_screen) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialFadeThrough()
        enterTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = LicensesScreenBinding.bind(view)
        binding.appBar.toolbar.setTitle(R.string.about_license_title)
        binding.appBar.toolbar.setupUpNavigation()

        @SuppressLint("SetTextI18n")
        binding.eventsStats.text = """Events count:
Persian Events: ${persianEvents.size + 1}
Islamic Events: ${islamicEvents.size + 1}
Gregorian Events: ${gregorianEvents.size + 1}
Nepali Events: ${nepaliEvents.size + 1}
Irregular Recurring Events: ${irregularRecurringEvents.size + 1}

Sources:
${enumValues<EventType>().joinToString("\n") { "${it.name}: ${it.source}" }}"""
        Linkify.addLinks(binding.eventsStats, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
        val sideSheet = SideSheetBehavior.from(binding.standardSideSheet)
        sideSheet.addCallback(object : SideSheetCallback() {
            override fun onSlide(sheet: View, slideOffset: Float) = Unit
            override fun onStateChanged(sheet: View, newState: Int) {
                if (newState == SideSheetBehavior.STATE_EXPANDED)
                    Handler(Looper.getMainLooper())
                        .postDelayed(TWO_SECONDS_IN_MILLIS) { sideSheet.hide() }
            }
        })

        binding.railView.menu.also {
            fun createTextIcon(text: String): Drawable {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.textSize = 40f
                val bounds = Rect()
                paint.color = Color.WHITE
                paint.getTextBounds(text, 0, text.length, bounds)
                val padding = 1 * resources.dp
                val width = bounds.width() + padding.toInt() * 2
                val height = bounds.height()
                val bitmap = createBitmap(width, height)
                    .applyCanvas { drawText(text, padding, height.toFloat(), paint) }
                return BitmapDrawable(view.context.resources, bitmap)
            }
            listOf(
                Triple(
                    "GPLv3",
                    view.context.getCompatDrawable(R.drawable.ic_info),
                    ::showShaderSandboxDialog
                ),
                Triple(
                    KotlinVersion.CURRENT.toString(),
                    createTextIcon("Kotlin"),
                    ::showSpringDemoDialog
                ),
                Triple(
                    "API ${Build.VERSION.SDK_INT}",
                    view.context.getCompatDrawable(R.drawable.ic_motorcycle),
                    ::showFlingDemoDialog
                ),
            ).forEach { (title, icon, dialog) ->
                val clickHandler = createEasterEggClickHandler(dialog)
                it.add(title).setIcon(icon).onClick { clickHandler(activity) }
            }
        }

        // Based on https://stackoverflow.com/a/34623367
        class BadgeSpan : ReplacementSpan() {
            private val sidePadding = 6 * resources.sp

            override fun getSize(
                paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?
            ) = (paint.measureText(text, start, end) + sidePadding * 2).roundToInt()

            override fun draw(
                canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int,
                y: Int, bottom: Int, paint: Paint
            ) {
                val verticalReduce = 5 * resources.sp
                val rect = RectF(
                    x, top + verticalReduce,
                    x + getSize(paint, text, start, end, null), bottom.toFloat()
                )
                paint.color =
                    view.context.resolveColor(com.google.android.material.R.attr.colorSurfaceDim)
                val dp = resources.dp
                canvas.drawRoundRect(rect, 25 * dp, 25 * dp, paint)
                paint.color = view.context.resolveColor(android.R.attr.textColorPrimary)
                canvas.drawText(text ?: "", start, end, x + sidePadding, y.toFloat(), paint)
            }
        }

        val sections = getCreditsSections().map { (title, license, text) ->
            buildSpannedString {
                append(title)
                if (license != null) {
                    append("  ")
                    scale(.7f) { inSpans(BadgeSpan()) { append(license) } }
                }
            } to SpannableString(text).also {
                Linkify.addLinks(it, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
            }
        }

        binding.recyclerView.adapter = ExpandableItemsAdapter(sections)
        val layoutManager = LinearLayoutManager(context)
        val itemDecoration = DividerItemDecoration(context, layoutManager.orientation)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addItemDecoration(itemDecoration)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.recyclerView.updatePadding(bottom = insets.bottom)
            binding.appBar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }
    }
}
