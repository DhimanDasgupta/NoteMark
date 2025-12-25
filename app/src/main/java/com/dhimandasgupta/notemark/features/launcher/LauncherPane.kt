package com.dhimandasgupta.notemark.features.launcher

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.common.extensions.setForcedDarkStatusBarIcons
import com.dhimandasgupta.notemark.ui.WindowSizePreviews
import com.dhimandasgupta.notemark.ui.common.DeviceLayoutType
import com.dhimandasgupta.notemark.ui.common.getDeviceLayoutType
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkButton
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkOutlinedButton
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun LauncherPane(
    modifier: Modifier = Modifier,
    launcherUiModel: () -> LauncherUiModel,
    navigateToAfterLogin: () -> Unit = {},
    navigateToLogin: () -> Unit = {},
    navigateToList: () -> Unit = {}
) {
    val context = LocalActivity.current
    SideEffect { context?.setForcedDarkStatusBarIcons(true) }

    val updatedLauncherUiModel by rememberUpdatedState(newValue = launcherUiModel)
    val updatedNavigateToList by rememberUpdatedState(navigateToList)
    val updatedNavigateToLogin by rememberUpdatedState(navigateToLogin)

    LaunchedEffect(key1 = Unit) {
        snapshotFlow { updatedLauncherUiModel().loggedInUser }
            .collect { loggedInUser ->
                if (loggedInUser != null) {
                    updatedNavigateToList()
                }
            }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.splash_blue_background))
    ) {
        when (val layoutType = getDeviceLayoutType()) {
            DeviceLayoutType.PHONE_PORTRAIT -> {
                LandingPanePortrait(
                    navigateToLogin = updatedNavigateToLogin,
                    deviceLayoutType = layoutType,
                    navigateToAfterLogin = navigateToAfterLogin
                )
            }

            DeviceLayoutType.PHONE_LANDSCAPE -> {
                LandingPaneLandscape(
                    navigateToLogin = updatedNavigateToLogin,
                    deviceLayoutType = layoutType,
                    navigateToAfterLogin = navigateToAfterLogin
                )
            }

            DeviceLayoutType.TABLET_LAYOUT -> {
                LandingPaneTablet(
                    navigateToLogin = updatedNavigateToLogin,
                    deviceLayoutType = layoutType,
                    navigateToAfterLogin = navigateToAfterLogin
                )
            }
        }
    }
}

@Composable
private fun LandingPanePortrait(
    modifier: Modifier = Modifier,
    deviceLayoutType: DeviceLayoutType,
    navigateToAfterLogin: () -> Unit = {},
    navigateToLogin: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_phone_portrait),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = modifier
                .aspectRatio(ratio = 0.8f)
                .align(Alignment.TopCenter)
        )

        ForegroundPane(
            modifier = modifier
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp
                    )
                )
                .background(color = colorScheme.surface)
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = WindowInsets
                        .displayCutout.union(insets = WindowInsets.navigationBars)
                        .asPaddingValues()
                        .calculateBottomPadding()
                ),
            navigateToLogin = navigateToLogin,
            deviceLayoutType = deviceLayoutType,
            navigateToAfterLogin = navigateToAfterLogin
        )
    }
}

@Composable
private fun LandingPaneLandscape(
    modifier: Modifier = Modifier,
    deviceLayoutType: DeviceLayoutType,
    navigateToAfterLogin: () -> Unit = {},
    navigateToLogin: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_phone_landscape),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = modifier.fillMaxHeight(0.65f)
        )

        ForegroundPane(
            modifier = modifier
                .wrapContentSize(
                    align = Alignment.Center
                )
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        bottomStart = 16.dp
                    )
                )
                .background(color = colorScheme.surface)
                .padding(
                    start = 32.dp,
                    top = 32.dp,
                    end = WindowInsets
                        .displayCutout.union(insets = WindowInsets.navigationBars)
                        .asPaddingValues()
                        .calculateRightPadding(LayoutDirection.Ltr),
                    bottom = 32.dp,
                )
                .fillMaxHeight(fraction = 0.85f),
            navigateToLogin = navigateToLogin,
            deviceLayoutType = deviceLayoutType,
            navigateToAfterLogin = navigateToAfterLogin
        )
    }
}

@Composable
private fun LandingPaneTablet(
    modifier: Modifier = Modifier,
    deviceLayoutType: DeviceLayoutType,
    navigateToAfterLogin: () -> Unit = {},
    navigateToLogin: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_phone_landscape),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .weight(weight = 1f)
                .aspectRatio(ratio = 0.5f)
        )

        ForegroundPane(
            modifier = modifier
                .fillMaxWidth(fraction = 0.85f)
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp
                    )
                )
                .background(color = colorScheme.surface)
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = WindowInsets
                        .navigationBars.union(insets = WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateBottomPadding()
                ),
            navigateToLogin = navigateToLogin,
            deviceLayoutType = deviceLayoutType,
            navigateToAfterLogin = navigateToAfterLogin
        )
    }
}

@Composable
private fun ForegroundPane(
    modifier: Modifier = Modifier,
    navigateToAfterLogin: () -> Unit = {},
    navigateToLogin: () -> Unit = {},
    deviceLayoutType: DeviceLayoutType
) {
    Column(
        modifier = modifier
            .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = when (deviceLayoutType) {
            DeviceLayoutType.TABLET_LAYOUT -> Alignment.CenterHorizontally
            else -> Alignment.Start
        }
    ) {
        Text(
            text = stringResource(R.string.landing_info_one),
            style = typography.titleLarge,
            color = colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .wrapContentSize(Alignment.Center)
        )

        Spacer(Modifier.height(height = 8.dp))

        Text(
            text = stringResource(R.string.landing_info_two),
            style = typography.bodyLarge,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .wrapContentSize(Alignment.Center)
        )

        Spacer(Modifier.height(height = 24.dp))

        NoteMarkButton(
            onClick = navigateToAfterLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = true
        ) {
            Text(
                text = "Get Started",
                style = typography.titleSmall,
                modifier = Modifier
                    .requiredHeight(IntrinsicSize.Min)
            )
        }

        Spacer(Modifier.height(height = 8.dp))

        NoteMarkOutlinedButton(
            onClick = { navigateToLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = true
        ) {
            Text(
                text = "Log in",
                style = typography.titleSmall,
                modifier = Modifier
                    .requiredHeight(IntrinsicSize.Min)
            )
        }
    }
}

@WindowSizePreviews
@Composable
private fun LauncherPanePreview() {
    NoteMarkTheme {
        LauncherPane(
            modifier = Modifier,
            launcherUiModel = { LauncherUiModel.Empty }
        )
    }
}