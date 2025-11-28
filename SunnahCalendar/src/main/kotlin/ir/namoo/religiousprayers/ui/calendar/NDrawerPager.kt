package ir.namoo.religiousprayers.ui.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.theme.isDynamicGrayscale
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun NDrawerPager() {
    var actualImage by remember {
        mutableIntStateOf(NavigationImage.fromDate(jdn = Jdn.today()).ordinal)
    }
    val pageSize = 200
    val pagerState = rememberPagerState(pageSize / 2 + actualImage, pageCount = { pageSize })

    LaunchedEffect(Unit) {
        while (true) {
            delay(30.seconds)
            val imageIndex = NavigationImage.fromDate(jdn = Jdn.today()).ordinal
            if (imageIndex != actualImage) {
                actualImage = imageIndex
                pagerState.animateScrollToPage(pageSize / 2 + actualImage)
            }
        }
    }

    val isDynamicGrayscale = isDynamicGrayscale()
    val imageFilter = remember(isDynamicGrayscale) {
        if (!isDynamicGrayscale) null
        // Consider gray scale themes of Android 14
        // And apply a gray scale filter https://stackoverflow.com/a/75698731
        else ColorFilter.colorMatrix(ColorMatrix().also { it.setToSaturation(0f) })
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, top = 12.dp, start = 20.dp, end = 20.dp)
            .height(170.dp)
            .clip(MaterialTheme.shapes.extraLarge),
        pageSpacing = 8.dp,
    ) {
        Image(
            ImageBitmap.imageResource(NavigationImage.entries[it % 4].imageID),
            contentScale = ContentScale.FillBounds,
            contentDescription = stringResource(id = NavigationImage.entries[it % 4].nameStringId),
            colorFilter = imageFilter,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.extraLarge),
        )
    }
}
