package com.byagowi.persiancalendar.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.compass.CompassScreen
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalSharedTransitionApi::class)
class CompassScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            CompassScreen({}, {}, {}, {}, scope)
        }
    }

    @Test
    fun navigateToLevelIsCalled() {
        var levelString = ""
        var navigateToLevelIsCalled = false
        composeTestRule.setContentWithParent { scope ->
            levelString = stringResource(R.string.level)
            CompassScreen(
                openDrawer = {},
                navigateToLevel = { navigateToLevelIsCalled = true },
                navigateToMap = { assert(false) },
                navigateToSettingsLocationTab = {},
                animatedContentScope = scope,
            )
        }
        assert(!navigateToLevelIsCalled)
        composeTestRule.onNodeWithContentDescription(levelString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToLevelIsCalled)
    }

    @Test
    fun navigateToMapIsCalled() {
        var mapString = ""
        var navigateToMapIsCalled = false
        composeTestRule.setContentWithParent { scope ->
            mapString = stringResource(R.string.map)
            CompassScreen(
                openDrawer = {},
                navigateToLevel = { assert(false) },
                navigateToMap = { navigateToMapIsCalled = true },
                navigateToSettingsLocationTab = {},
                animatedContentScope = scope,
            )
        }
        assert(!navigateToMapIsCalled)
        composeTestRule.onNodeWithContentDescription(mapString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToMapIsCalled)
    }
}
