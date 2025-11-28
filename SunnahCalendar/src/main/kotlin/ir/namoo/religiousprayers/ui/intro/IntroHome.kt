package ir.namoo.religiousprayers.ui.intro

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.AddLocation
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import ir.namoo.commons.utils.isNetworkConnected
import kotlinx.coroutines.launch

@Composable
fun IntroHomeScreen(
    modifier: Modifier = Modifier, startMainActivity: () -> Unit
) {
    val context = LocalContext.current
    var isNetworkConnected by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> isNetworkConnected = isNetworkConnected(context)
                Lifecycle.Event.ON_CREATE -> {}
                Lifecycle.Event.ON_START -> {}
                Lifecycle.Event.ON_RESUME -> isNetworkConnected = isNetworkConnected(context)
                Lifecycle.Event.ON_STOP -> {}
                Lifecycle.Event.ON_DESTROY -> {}
                Lifecycle.Event.ON_ANY -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val tabs = listOf(
        IntroTabs(
            title = stringResource(id = R.string.welcome),
            outlinedIcon = Icons.Outlined.Home,
            filledIcon = Icons.Filled.Home
        ),
        IntroTabs(
            title = stringResource(id = R.string.select_city),
            outlinedIcon = Icons.Outlined.LocationOn,
            filledIcon = Icons.Filled.LocationOn
        ),
        IntroTabs(
            title = stringResource(id = R.string.custom_location),
            outlinedIcon = Icons.Outlined.AddLocation,
            filledIcon = Icons.Filled.AddLocation
        ),
    )
    val pagerState = rememberPagerState(
        initialPage = IntroScreen.Welcome.ordinal, pageCount = IntroScreen.entries::size
    )
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        modifier = modifier.fillMaxSize(), topBar = {}) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                contentColor = LocalContentColor.current,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {
                    val isLandscape =
                        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                    TabRowDefaults.PrimaryIndicator(
                        Modifier.tabIndicatorOffset(pagerState.currentPage),
                        width = if (isLandscape) 92.dp else 64.dp,
                        color = LocalContentColor.current.copy(alpha = AppBlendAlpha)
                    )
                },
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = index == pagerState.currentPage
                    Tab(icon = { tab.Icon(selected) }, text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }, selected = selected, onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    })
                }
            }
            Surface(
                modifier = Modifier.fillMaxSize(), shape = materialCornerExtraLargeTop()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = !isNetworkConnected,
                        enter = slideInVertically(),
                        exit = shrinkVertically()
                    ) {
                        ElevatedCard(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = MaterialTheme.shapes.extraLarge,
                            onClick = { isNetworkConnected = isNetworkConnected(context) }) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                text = stringResource(id = R.string.network_error_message) + "\n" + stringResource(
                                    id = R.string.recheck
                                ),
                                textAlign = TextAlign.Center,
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    HorizontalPager(state = pagerState) { index ->
                        IntroHomeContent(
                            screen = IntroScreen.entries[index],
                            startMainActivity = startMainActivity,
                            goToDownloadLocation = {
                                isNetworkConnected = isNetworkConnected(context)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(IntroScreen.DownloadLocation.ordinal)
                                }
                            },
                            goToCustomLocation = {
                                isNetworkConnected = isNetworkConnected(context)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(IntroScreen.CustomLocation.ordinal)
                                }
                            })
                    }
                }
            }
        }
    }
}

@Immutable
data class IntroTabs(
    val title: String, private val outlinedIcon: ImageVector, private val filledIcon: ImageVector,
) {
    @Composable
    fun Icon(isSelected: Boolean) {
        Crossfade(isSelected, label = "icon") {
            Icon(if (it) filledIcon else outlinedIcon, contentDescription = null)
        }
    }
}


@Composable
fun IntroHomeContent(
    screen: IntroScreen,
    startMainActivity: () -> Unit,
    goToDownloadLocation: () -> Unit,
    goToCustomLocation: () -> Unit
) {
    when (screen) {
        IntroScreen.Welcome -> IntroWelcomeScreen(goToDownloadLocation = goToDownloadLocation)
        IntroScreen.DownloadLocation -> IntroDownloadLocationScreen(
            startMainActivity = startMainActivity, goToCustomLocation = goToCustomLocation
        )

        IntroScreen.CustomLocation -> IntroCustomLocationScreen(startMainActivity)
    }
}
