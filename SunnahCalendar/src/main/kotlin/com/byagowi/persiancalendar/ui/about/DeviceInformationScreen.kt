package com.byagowi.persiancalendar.ui.about

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import android.hardware.Sensor
import android.hardware.SensorManager
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES10
import android.opengl.GLES20
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.InputDevice
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.getSystemService
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DeviceInformationItemBinding
import com.byagowi.persiancalendar.databinding.DeviceInformationScreenBinding
import com.byagowi.persiancalendar.ui.utils.copyToClipboard
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.ui.utils.shareTextFile
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.circularreveal.CircularRevealCompat
import com.google.android.material.circularreveal.CircularRevealWidget
import com.google.android.material.snackbar.Snackbar
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe
import java.util.Locale
import kotlin.math.hypot

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class DeviceInformationScreen : Fragment(R.layout.device_information_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DeviceInformationScreenBinding.bind(view)
        binding.toolbar.setTitle(R.string.device_information)
        binding.toolbar.setupUpNavigation()

        binding.root.circularRevealFromMiddle()

        binding.recyclerView.let {
            it.setHasFixedSize(true)
            it.layoutManager = LinearLayoutManager(view.context)
            it.addItemDecoration(
                DividerItemDecoration(view.context, LinearLayoutManager.VERTICAL)
            )
            val adapter = DeviceInformationAdapter(activity ?: return@let)
            it.adapter = adapter
            binding.toolbar.menu.add(R.string.share).also { menu ->
                menu.icon = binding.toolbar.context.getCompatDrawable(R.drawable.ic_baseline_share)
                menu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }.onClick {
                activity?.shareTextFile(
                    adapter.asHtml(),
                    "device.html",
                    "text/html"
                )
            }
            binding.toolbar.menu.add("Print").also { menu ->
                menu.setIcon(R.drawable.ic_print)
                menu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }.onClick { context?.openHtmlInBrowser(adapter.asHtml()) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.toolbar.menu.add("Game").also {
                it.icon = binding.toolbar.context.getCompatDrawable(R.drawable.ic_esports)
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }.onClick {
                runCatching {
                    startActivity(
                        Intent(Intent.ACTION_MAIN).setClassName(
                            "com.android.systemui", "com.android.systemui.egg.MLandActivity"
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }.onFailure(logException).onFailure {
                    Snackbar.make(
                        binding.root,
                        R.string.device_does_not_support,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.bottomNavigation.menu.also {
            listOf(
                Triple(R.drawable.ic_developer, Build.VERSION.RELEASE, ::showHiddenUiDialog),
                Triple(
                    R.drawable.ic_settings,
                    "API " + Build.VERSION.SDK_INT,
                    ::showSensorTestDialog
                ),
                Triple(
                    R.drawable.ic_motorcycle,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) Build.SUPPORTED_ABIS[0]
                    else @Suppress("DEPRECATION") Build.CPU_ABI,
                    ::showInputDeviceTestDialog
                ),
                Triple(
                    R.drawable.ic_device_information_white,
                    Build.MODEL,
                    ::showColorPickerDialog
                ),
            ).forEach { (icon, title, dialog) ->
                val clickHandler = createEasterEggClickHandler(dialog)
                it.add(title).setIcon(icon).onClick { clickHandler(activity) }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            binding.bottomNavigation.updatePadding(bottom = insets.bottom)
            binding.recyclerView.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}

// https://stackoverflow.com/a/52557989
fun <T> T.circularRevealFromMiddle() where T : View?, T : CircularRevealWidget {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
    post {
        val viewWidth = width
        val viewHeight = height
        val diagonal = hypot(viewWidth.toDouble(), viewHeight.toDouble()).toInt()
        AnimatorSet().also {
            it.playTogether(
                CircularRevealCompat.createCircularReveal(
                    this, viewWidth / 2f, viewHeight / 2f, 10f, diagonal / 2f
                ),
                ObjectAnimator.ofArgb(
                    this,
                    CircularRevealWidget.CircularRevealScrimColorProperty.CIRCULAR_REVEAL_SCRIM_COLOR,
                    Color.GRAY, Color.TRANSPARENT
                )
            )
            it.duration = 500
        }.start()
    }
}

// @Language("AGSL")
// private const val demoRuntimeShader = """
// // This is a SkSl/AGSL flavor shader only usable in Android 13, see also:
// // * https://shaders.skia.org/?id=de2a4d7d893a7251eb33129ddf9d76ea517901cec960db116a1bbd7832757c1f
// // * https://developer.android.com/about/versions/13/features#graphics
// // * https://cs.android.com/android/platform/superproject/+/master:external/skia/src/sksl/SkSLMain.cpp;l=275
//
// uniform float iTime;
// uniform vec2 iResolution;
//
// // Source: @notargs https://twitter.com/notargs/status/1250468645030858753
// half4 main(vec2 fragCoord) {
//     vec3 d = .5 - fragCoord.xy1 / iResolution.y, p = vec3(0), o;
//     for (int i = 0; i < 32; ++i) {
//         o = p;
//         o.z -= iTime * 9.;
//         float a = o.z * .1;
//         o.xy *= mat2(cos(a), sin(a), -sin(a), cos(a));
//         p += (.1 - length(cos(o.xy) + sin(o.yz))) * d;
//     }
//     return ((sin(p) + vec3(2, 5, 12)) / length(p)).xyz1;
// }
// """

class CheckerBoard(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val checkerBoard = createCheckerRoundedBoard(40f, 8f, Color.parseColor("#100A0A0A"))
    // private val startTime = System.nanoTime()
    // private val shader by lazy(LazyThreadSafetyMode.NONE) {
    //     runCatching {
    //         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@runCatching null
    //         RuntimeShader(demoRuntimeShader).also {
    //             val width = context.resources?.displayMetrics?.widthPixels?.toFloat() ?: 800f
    //             val height = context.resources?.displayMetrics?.heightPixels?.toFloat() ?: 800f
    //             it.setFloatUniform("iResolution", width, height)
    //         }
    //     }.onFailure(logException).getOrNull().debugAssertNotNull
    // }
    // private val shaderPaint = Paint().also {
    //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    //         it.shader = shader
    //     }
    // }

    override fun onDraw(canvas: Canvas) {
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //     shader?.setFloatUniform("iTime", (System.nanoTime() - startTime) / 1e9f)
        //     canvas.drawPaint(shaderPaint)
        //     invalidate()
        // } else
        canvas.drawPaint(checkerBoard)
    }
}

// https://stackoverflow.com/a/58471997
@Suppress("SameParameterValue")
private fun createCheckerRoundedBoard(
    tileSize: Float, r: Float, @ColorInt color: Int
) = Paint(Paint.ANTI_ALIAS_FLAG).also { paint ->
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@also
    val tileSize2x = tileSize.toInt() * 2
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = color
    }
    val bitmap = createBitmap(tileSize2x, tileSize2x).applyCanvas {
        drawRoundRect(0f, 0f, tileSize, tileSize, r, r, fill)
        drawRoundRect(tileSize, tileSize, tileSize * 2f, tileSize * 2f, r, r, fill)
    }
    paint.shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
}


// https://stackoverflow.com/a/68822715
// instead android.text.format.Formatter.formatShortFileSize() to control its locale
private fun humanReadableByteCountBin(bytes: Long): String = when {
    bytes >= 1 shl 30 -> "%.1f GB".format(Locale.ENGLISH, bytes.toDouble() / (1 shl 30))
    bytes >= 1 shl 20 -> "%.1f MB".format(Locale.ENGLISH, bytes.toDouble() / (1 shl 20))
    bytes >= 1 shl 10 -> "%.0f kB".format(Locale.ENGLISH, bytes.toDouble() / (1 shl 10))
    else -> "$bytes bytes"
}

private class DeviceInformationAdapter(private val activity: FragmentActivity) :
    ListAdapter<DeviceInformationAdapter.Item, DeviceInformationAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(old: Item, new: Item) = old.title == new.title
            override fun areContentsTheSame(old: Item, new: Item) = old == new
        }
    ) {

    data class Item(val title: String, val content: CharSequence?, val version: String = "")

    private val deviceInformationItems = listOf(
        Item(
            "CPU Instructions Sets", (when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> Build.SUPPORTED_ABIS
                else -> @Suppress("DEPRECATION") arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
            }).joinToString(", ")
        ),
        Item(
            "Android Version", Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT.toString()
        ),
        Item("Model", Build.MODEL),
        Item("Product", Build.PRODUCT),
        Item(
            "Screen Resolution", activity.windowManager?.let {
                "%d*%d pixels".format(
                    Locale.ENGLISH,
                    activity.resources?.displayMetrics?.widthPixels ?: 0,
                    activity.resources?.displayMetrics?.heightPixels ?: 0
                )
            }, "%.1fHz".format(
                Locale.ENGLISH, when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> activity.display?.refreshRate
                    else ->
                        @Suppress("DEPRECATION")
                        activity.windowManager?.defaultDisplay?.refreshRate
                } ?: ""
            )
        ),
        Item("DPI", activity.resources?.displayMetrics?.densityDpi?.toString()),
        Item("Available Processors", Runtime.getRuntime()?.availableProcessors()?.toString()),
        Item("Instruction Architecture", Build.DEVICE),
        Item("Manufacturer", Build.MANUFACTURER),
        Item("Brand", Build.BRAND),
        Item("Android Id", Build.ID),
        Item("Board", Build.BOARD),
        Item("Radio Firmware Version", Build.getRadioVersion()),
        Item("Build User", Build.USER),
        Item("Host", Build.HOST),
        Item("Boot Loader", Build.BOOTLOADER),
        Item("Device", Build.DEVICE),
        Item("Tags", Build.TAGS),
        Item("Hardware", Build.HARDWARE),
        Item("Type", Build.TYPE),
        Item("Display", Build.DISPLAY),
        Item("Device Fingerprints", Build.FINGERPRINT),
        Item(
            "RAM", humanReadableByteCountBin(ActivityManager.MemoryInfo().also {
                activity.getSystemService<ActivityManager>()?.getMemoryInfo(it)
            }.totalMem)
        ),
        Item(
            "Battery", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                activity.getSystemService<BatteryManager>()?.let {
                    listOf("Charging: ${it.isCharging}") + listOf(
                        "Capacity" to BatteryManager.BATTERY_PROPERTY_CAPACITY,
                        "Charge Counter" to BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER,
                        "Current Avg" to BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE,
                        "Current Now" to BatteryManager.BATTERY_PROPERTY_CURRENT_NOW,
                        "Energy Counter" to BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER
                    ).map { (title: String, id: Int) -> "$title: ${it.getLongProperty(id)}" }
                }?.joinToString("\n") else ""
        ),
        Item("App Standby Bucket", appStandbyStatus(activity)),
        Item("Display Metrics", activity.resources?.displayMetrics?.toString() ?: ""),
        Item(
            "Display Cutout", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) run {
                val cutout = activity.window?.decorView?.rootWindowInsets?.displayCutout
                    ?: return@run "None"
                listOf(
                    "Safe Inset Top" to cutout.safeInsetTop,
                    "Safe Inset Right" to cutout.safeInsetRight,
                    "Safe Inset Bottom" to cutout.safeInsetBottom,
                    "Safe Inset Left" to cutout.safeInsetLeft,
                    "Rects" to cutout.boundingRects.joinToString(",")
                ).joinToString("\n") { (key, value) -> "$key: $value" }
            } else "None"
        ),
        Item(
            "Install Source of ${activity.packageName}", runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    activity.packageManager?.getInstallSourceInfo(activity.packageName)?.run {
                        """
                        |Initiating Package Name: $initiatingPackageName
                        |Installing Package Name: $installingPackageName
                        |Originating Package Name: $originatingPackageName
                        |Initiating Package Signing Info: $initiatingPackageSigningInfo
                        |Installer Package Name: ${
                            @Suppress("DEPRECATION")
                            activity.packageManager?.getInstallerPackageName(activity.packageName) ?: ""
                        }
                        """.trimMargin("|").trim()
                    }
                } else
                    @Suppress("DEPRECATION")
                    activity.packageManager?.getInstallerPackageName(activity.packageName) ?: ""
            }.onFailure(logException).getOrNull()
        ),
        Item(
            "Sensors", activity.getSystemService<SensorManager>()
                ?.getSensorList(Sensor.TYPE_ALL)?.joinToString("\n")
        ),
        Item("Input Device", InputDevice.getDeviceIds().map(InputDevice::getDevice).joinToString()),
        Item(
            "System Features",
            activity.packageManager?.systemAvailableFeatures?.joinToString("\n")
        )
    ) + (runCatching {
        // Quick Kung-fu to create gl context, https://stackoverflow.com/a/27092070
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val versions = IntArray(2)
        EGL14.eglInitialize(display, versions, 0, versions, 1)
        val configAttr = intArrayOf(
            EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
            EGL14.EGL_LEVEL, 0, EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT, EGL14.EGL_NONE
        )
        val configs = Array<EGLConfig?>(1) { null }
        val configsCount = IntArray(1)
        EGL14.eglChooseConfig(display, configAttr, 0, configs, 0, 1, configsCount, 0)
        if (configsCount[0] != 0) {
            val surf = EGL14.eglCreatePbufferSurface(
                display, configs[0],
                intArrayOf(EGL14.EGL_WIDTH, 64, EGL14.EGL_HEIGHT, 64, EGL14.EGL_NONE), 0
            )
            EGL14.eglMakeCurrent(
                display, surf, surf, EGL14.eglCreateContext(
                    display, configs[0], EGL14.EGL_NO_CONTEXT,
                    intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE), 0
                )
            )
        }
        listOf(
            Item(
                "OpenGL", (listOf(
                    "GL_VERSION" to GLES20.GL_VERSION, "GL_RENDERER" to GLES20.GL_RENDERER,
                    "GL_VENDOR" to GLES20.GL_VENDOR
                ).map { (title: String, id: Int) -> "$title: ${GLES20.glGetString(id)}" } + listOf(
                    "GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS" to GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS,
                    "GL_MAX_CUBE_MAP_TEXTURE_SIZE" to GLES20.GL_MAX_CUBE_MAP_TEXTURE_SIZE,
                    "GL_MAX_FRAGMENT_UNIFORM_VECTORS" to GLES20.GL_MAX_FRAGMENT_UNIFORM_VECTORS,
                    "GL_MAX_RENDERBUFFER_SIZE" to GLES20.GL_MAX_RENDERBUFFER_SIZE,
                    "GL_MAX_TEXTURE_IMAGE_UNITS" to GLES20.GL_MAX_TEXTURE_IMAGE_UNITS,
                    "GL_MAX_TEXTURE_SIZE" to GLES20.GL_MAX_TEXTURE_SIZE,
                    "GL_MAX_VARYING_VECTORS" to GLES20.GL_MAX_VARYING_VECTORS,
                    "GL_MAX_VERTEX_ATTRIBS" to GLES20.GL_MAX_VERTEX_ATTRIBS,
                    "GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS" to GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS,
                    "GL_MAX_VERTEX_UNIFORM_VECTORS" to GLES20.GL_MAX_VERTEX_UNIFORM_VECTORS,
                    "GL_MAX_VIEWPORT_DIMS" to GLES20.GL_MAX_VIEWPORT_DIMS
                ).map { (title: String, id: Int) ->
                    val intBuffer = IntArray(1)
                    GLES10.glGetIntegerv(id, intBuffer, 0)
                    "$title: ${intBuffer[0]}"
                }).joinToString("\n")
            ),
            Item(
                "OpenGL Extensions", buildSpannedString {
                    val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS).trim().split(" ")
                    val regex = Regex("GL_([a-zA-Z]+)_(.+)")
                    extensions.forEachIndexed { i, it ->
                        if (i != 0) appendLine()

                        if (!regex.matches(it)) append(it)
                        else inSpans(object : ClickableSpan() {
                            override fun onClick(textView: View) {
                                runCatching {
                                    val pattern =
                                        "https://www.khronos.org/registry/OpenGL/extensions/$1/$1_$2.txt"
                                    CustomTabsIntent.Builder().build().launchUrl(
                                        activity, it.replace(regex, pattern).toUri()
                                    )
                                }.onFailure(logException)
                            }
                        }) { append(it) }
                    }
                }
            )
        )
    }.onFailure(logException).getOrDefault(emptyList()))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        DeviceInformationItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = deviceInformationItems.size

    fun asHtml() = createHTML().html {
        head {
            meta(charset = "utf8")
            style { unsafe { +"td { padding: .5em; border-top: 1px solid lightgray }" } }
        }
        body {
            h1 { +"Device Information" }
            table {
                thead { tr { th { +"Item" }; th { +"Value" } } }
                tbody {
                    deviceInformationItems.forEach {
                        tr {
                            th { +(it.title + if (it.version.isEmpty()) "" else " (${it.version})") }
                            th { +it.content.toString() }
                        }
                    }
                }
            }
            script { unsafe { +"print()" } }
        }
    }

    inner class ViewHolder(private val binding: DeviceInformationItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }

        fun bind(position: Int) {
            deviceInformationItems[position].also {
                binding.title.text = it.title
                binding.content.text = it.content ?: "Unknown"
                binding.version.text = it.version
            }
            binding.content.movementMethod = LinkMovementMethod.getInstance()
        }

        override fun onClick(v: View?) =
            activity.copyToClipboard(deviceInformationItems[bindingAdapterPosition].content)
    }
}
