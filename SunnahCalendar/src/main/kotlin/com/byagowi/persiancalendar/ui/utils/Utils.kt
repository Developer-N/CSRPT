package com.byagowi.persiancalendar.ui.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.variants.debugLog
import java.io.ByteArrayOutputStream
import java.io.File

inline val Resources.isRtl get() = configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL || language.value.isLessKnownRtl
inline val Resources.isLandscape get() = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
inline val Resources.dp: Float get() = displayMetrics.density

fun Context.bringMarketPage() {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
    }.onFailure(logException).onFailure {
        runCatching {
            val uri = "https://play.google.com/store/apps/details?id=$packageName".toUri()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure(logException)
    }
}

fun Bitmap.toPngByteArray(): ByteArray {
    val buffer = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, buffer)
    return buffer.toByteArray()
}

//fun Bitmap.toPngBase64(): String =
//    "data:image/png;base64," + Base64.encodeToString(toByteArray(), Base64.DEFAULT)

private inline fun Context.saveAsFile(fileName: String, crossinline action: (File) -> Unit): Uri {
    return FileProvider.getUriForFile(
        applicationContext, "$packageName.provider", File(externalCacheDir, fileName).also(action)
    )
}

fun Context.openHtmlInBrowser(html: String) {
    runCatching {
        CustomTabsIntent.Builder().build()
            .also { it.intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            .launchUrl(this, saveAsFile("persian-calendar.html") { it.writeText(html) })
    }.onFailure(logException)
}

fun Context.shareText(text: String, chooserTitle: String) {
    runCatching {
        ShareCompat.IntentBuilder(this).setType("text/plain").setChooserTitle(chooserTitle)
            .setText(text).startChooser()
    }.onFailure(logException)
}

private fun Context.shareUriFile(uri: Uri, mime: String) {
    runCatching {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).also {
            it.type = mime
            it.putExtra(Intent.EXTRA_STREAM, uri)
        }, getString(R.string.share)))
    }.onFailure(logException)
}

fun Context.shareTextFile(text: String, fileName: String, mime: String) =
    shareUriFile(saveAsFile(fileName) { it.writeText(text) }, mime)

fun Context.shareBinaryFile(binary: ByteArray, fileName: String, mime: String) =
    shareUriFile(saveAsFile(fileName) { it.writeBytes(binary) }, mime)

// https://stackoverflow.com/a/58249983
// Akin to https://github.com/material-components/material-components-android/blob/8938da8c/lib/java/com/google/android/material/internal/ContextUtils.java#L40
tailrec fun Context.getActivity(): ComponentActivity? =
    this as? ComponentActivity ?: (this as? ContextWrapper)?.baseContext?.getActivity()

fun createFlingDetector(
    context: Context, callback: (velocityX: Float, velocityY: Float) -> Boolean
): GestureDetector {
    class FlingListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
        ): Boolean = callback(velocityX, velocityY)
    }

    return GestureDetector(context, FlingListener())
}

/**
 * Similar to [androidx.compose.foundation.isSystemInDarkTheme] implementation but
 * for non composable contexts, in composable context, use the compose one.
 */
fun isSystemInDarkTheme(configuration: Configuration): Boolean =
    configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

// Android 14 has a grayscale dynamic colors mode and this is somehow a hack to check for that
// I guess there will be better ways to check for that in the future I guess but this does the trick
// Android 13, at least in Extension 5 emulator image, also provides such theme.
// https://stackoverflow.com/a/76272434
val Resources.isDynamicGrayscale: Boolean
    get() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        val hsv = FloatArray(3)
        return listOf(
            android.R.color.system_accent1_500,
            android.R.color.system_accent2_500,
            android.R.color.system_accent3_500,
        ).all { Color.colorToHSV(getColor(it, null), hsv); hsv[1] < .25 }
    }

fun View.performHapticFeedbackVirtualKey() {
    debugLog("Preformed a haptic feedback virtual key")
    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
}

fun HapticFeedback.performLongPress() {
    debugLog("Preformed a haptic feedback long press")
    performHapticFeedback(HapticFeedbackType.LongPress)
}
