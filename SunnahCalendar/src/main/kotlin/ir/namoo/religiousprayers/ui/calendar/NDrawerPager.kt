package ir.namoo.religiousprayers.ui.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.theme
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NDrawerPager(drawerState: DrawerState) {
    var actualImage by remember {
        mutableIntStateOf(NavigationImage.fromDate(jdn = Jdn.today()).ordinal)
    }
    val pageSize = 200
    val imageState = rememberPagerState(
        initialPage = pageSize / 2 + actualImage - 3, // minus 3 so it does an initial animation
        pageCount = { pageSize },
    )
    if (drawerState.isOpen) {
        LaunchedEffect(Unit) {
            imageState.animateScrollToPage(pageSize / 2 + actualImage)
            while (true) {
                delay(THIRTY_SECONDS_IN_MILLIS)
                val imageIndex = NavigationImage.fromDate(jdn = Jdn.today()).ordinal
                if (imageIndex != actualImage) {
                    actualImage = imageIndex
                    imageState.animateScrollToPage(pageSize / 2 + actualImage)
                }
            }
        }
    }

    val context = LocalContext.current
    val theme by theme.collectAsState()
    val imageFilter = remember(LocalConfiguration.current, theme) {
        // Consider gray scale themes of Android 14
        // And apply a gray scale filter https://stackoverflow.com/a/75698731
        if (theme.isDynamicColors() && context.resources.isDynamicGrayscale) {
            ColorFilter.colorMatrix(ColorMatrix().also { it.setToSaturation(0f) })
        } else null
    }

    HorizontalPager(
        state = imageState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
            .height(196.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .semantics {
                @OptIn(ExperimentalComposeUiApi::class) this.invisibleToUser()
            },
        pageSpacing = 8.dp,
    ) {
        Image(
            ImageBitmap.imageResource(NavigationImage.entries[it % 4].imageID),
            contentScale = ContentScale.FillWidth,
            contentDescription = stringResource(id = NavigationImage.entries[it % 4].nameStringId),
            colorFilter = imageFilter,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.extraLarge),
        )
    }
}
