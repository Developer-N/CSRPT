package ir.namoo.religiousprayers.ui.intro

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Down
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Up
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.byagowi.persiancalendar.R
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.colorAppBar
import ir.namoo.commons.utils.colorOnAppBar
import ir.namoo.commons.utils.isNetworkConnected
import org.koin.androidx.compose.koinViewModel

@Composable
fun IntroHomeScreen(
    modifier: Modifier = Modifier,
    viewModel: IntroHomeViewModel = koinViewModel(),
    startMainActivity: () -> Unit
) {
    val context = LocalContext.current
    val selected by viewModel.selectedScreen.collectAsState()
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

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(
                        if (isSystemInDarkTheme()) 0.3f
                        else 1f
                    ),
                alignment = Alignment.BottomCenter,
                contentScale = ContentScale.FillBounds,
                painter = painterResource(id = R.drawable.islamic_background),
                contentDescription = "Islamic Photo"
            )
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            IntroHomeTabs(
                titles = listOf(
                    stringResource(id = R.string.welcome),
                    stringResource(id = R.string.select_city),
                    stringResource(id = R.string.custom_location)
                ).zip(
                    listOf(
                        Icons.Filled.Home,
                        Icons.Filled.LocationCity,
                        Icons.Filled.LocationOn,
                    )
                ), tabSelected = selected
            ) {
                isNetworkConnected = isNetworkConnected(context)
                viewModel.selectScreen(it)
            }
            AnimatedVisibility(
                visible = !isNetworkConnected,
                enter = slideInVertically(),
                exit = shrinkVertically()
            ) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(8.dp)
                        .clickable {
                            isNetworkConnected = isNetworkConnected(context)
                        },
                    text = stringResource(id = R.string.network_error_message),
                    fontFamily = FontFamily(appFont),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                )
            }

            IntroHomeContent(screen = selected,
                startMainActivity = startMainActivity,
                goToDownloadLocation = {
                    isNetworkConnected = isNetworkConnected(context)
                    viewModel.selectScreen(IntroScreen.DownloadLocation)
                },
                goToCustomLocation = {
                    isNetworkConnected = isNetworkConnected(context)
                    viewModel.selectScreen(IntroScreen.CustomLocation)
                })
        }
    }

}

@Composable
fun IntroHomeTabs(
    modifier: Modifier = Modifier,
    titles: List<Pair<String, ImageVector>>,
    tabSelected: IntroScreen,
    onTabSelected: (IntroScreen) -> Unit
) {
    TabRow(selectedTabIndex = tabSelected.ordinal,
        modifier = modifier,
        containerColor = colorAppBar,
        contentColor = colorOnAppBar,
        indicator = { tabPositions: List<TabPosition> ->
            Box(
                Modifier
                    .tabIndicatorOffset(tabPositions[tabSelected.ordinal])
                    .fillMaxSize()
                    .padding(top = 30.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .drawBehind {
                        val startX = size.width / 2 - size.width / 2
                        val endX = size.width / 2 + size.width / 2
                        drawLine(
                            color = colorOnAppBar,
                            start = Offset(startX, size.height - 5),
                            end = Offset(endX, size.height - 5),
                            strokeWidth = 20f,
                            cap = StrokeCap.Round
                        )
                        drawRoundRect(
                            color = colorOnAppBar, alpha = 0.2f
                        )
                    })
        },
        divider = { }) {
        titles.forEachIndexed { index, title ->
            val selected = index == tabSelected.ordinal
            Tab(modifier = Modifier
                .padding(top = 35.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
                .clip(MaterialTheme.shapes.extraLarge),
                selected = selected,
                onClick = {
                    onTabSelected(IntroScreen.values()[index])
                }) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = selected,
                        enter = slideInHorizontally(),
                        exit = shrinkHorizontally()
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                            text = title.first,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            fontFamily = FontFamily(appFont)
                        )
                    }
                    Icon(
                        imageVector = title.second,
                        contentDescription = title.first,
                        tint = colorOnAppBar
                    )
                }
            }
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
    AnimatedContent(targetState = screen, label = "intro_content", transitionSpec = {
        slideIntoContainer(
            animationSpec = tween(300, easing = EaseIn), towards = Up
        ).togetherWith(
            slideOutOfContainer(
                animationSpec = tween(300, easing = EaseOut), towards = Down
            )
        )
    }) { targetScreen ->
        when (targetScreen) {
            IntroScreen.DownloadLocation -> IntroDownloadLocationScreen(
                startMainActivity = startMainActivity, goToCustomLocation = goToCustomLocation
            )

            IntroScreen.CustomLocation -> IntroCustomLocationScreen(startMainActivity)
            IntroScreen.Welcome -> IntroWelcomeScreen(goToDownloadLocation = goToDownloadLocation)
        }
    }
}
