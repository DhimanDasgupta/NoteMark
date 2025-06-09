package com.dhimandasgupta.notemark.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.ui.common.DeviceLayoutType
import com.dhimandasgupta.notemark.ui.common.PhoneLandscapePreview
import com.dhimandasgupta.notemark.ui.common.TabletExpandedPreview
import com.dhimandasgupta.notemark.ui.common.TabletMediumPreview
import com.dhimandasgupta.notemark.ui.common.getDeviceLayoutType
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkButton
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkOutlinedButton

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun LauncherPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    navigateToAfterLogin: () -> Unit = {},
    navigateToLogin: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.splash_blue_background))
    ) {
        val layoutType = getDeviceLayoutType(windowSizeClass)

        when(layoutType) {
            DeviceLayoutType.PHONE_PORTRAIT -> {
                LandingPanePortrait(
                    navigateToLogin = navigateToLogin
                )
            }

            DeviceLayoutType.PHONE_LANDSCAPE -> {
                LandingPaneLandscape(
                    navigateToLogin = navigateToLogin
                )
            }

            DeviceLayoutType.TABLET_LAYOUT -> {
                LandingPaneTablet(
                    navigateToLogin = navigateToLogin
                )
            }
        }
    }
}

@Composable
private fun LandingPanePortrait(
    modifier: Modifier = Modifier,
    navigateToLogin: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_phone_portrait),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = modifier.aspectRatio(0.66f)
        )

        Column(
            modifier = modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp
                    )
                )
                .background(colorScheme.background)
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = WindowInsets
                        .displayCutout.union(WindowInsets.navigationBars)
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.landing_info_one),
                style = typography.headlineLarge,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .wrapContentSize(Alignment.Center)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.landing_info_two),
                style = typography.bodySmall,
                modifier = modifier
                    .wrapContentSize()
                    .padding(horizontal = 16.dp)
                    .wrapContentSize(Alignment.Center)
            )

            Spacer(Modifier.height(32.dp))

            NoteMarkButton(
                onClick = {},
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Get Started")
            }

            Spacer(Modifier.height(8.dp))

            NoteMarkOutlinedButton(
                onClick = { navigateToLogin() },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Log in")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LandingPaneLandscape(
    modifier: Modifier = Modifier,
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
            modifier = modifier.fillMaxHeight()
        )

        Column (
            modifier = modifier
                .wrapContentSize(
                    align = Alignment.Center
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        bottomStart = 16.dp
                    )
                )
                .background(colorScheme.background)
                .padding(
                    start = 32.dp,
                    top = 32.dp,
                    end = WindowInsets
                        .displayCutout.union(WindowInsets.navigationBars)
                        .asPaddingValues()
                        .calculateRightPadding(LayoutDirection.Ltr),
                    bottom = 32.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.landing_info_one),
                style = typography.headlineLarge,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .wrapContentSize(Alignment.Center)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.landing_info_two),
                style = typography.bodySmall,
                modifier = modifier
                    .wrapContentSize()
                    .padding(horizontal = 16.dp)
                    .wrapContentSize(Alignment.Center)
            )

            Spacer(Modifier.height(8.dp))

            NoteMarkButton(
                onClick = {},
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Get Started")
            }

            Spacer(Modifier.height(8.dp))

            NoteMarkOutlinedButton(
                onClick = { navigateToLogin() },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Log in")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LandingPaneTablet(
    modifier: Modifier = Modifier,
    navigateToLogin: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_tablet),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .aspectRatio(0.62f)
        )

        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = modifier
                .fillMaxWidth(0.75f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp
                    )
                )
                .background(colorScheme.background)
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = WindowInsets
                        .navigationBars.union(WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.landing_info_one),
                style = typography.headlineLarge,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .wrapContentSize(Alignment.Center)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.landing_info_two),
                style = typography.bodySmall,
                modifier = modifier
                    .wrapContentSize()
                    .padding(horizontal = 16.dp)
                    .wrapContentSize(Alignment.Center)
            )

            Spacer(Modifier.height(8.dp))

            NoteMarkButton(
                onClick = {},
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Get Started")
            }

            Spacer(Modifier.height(8.dp))

            NoteMarkOutlinedButton(
                onClick = { navigateToLogin() },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Log in")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Tablet Landscape", showSystemUi = true)
@Composable
private fun PreviewTabletLandscapeDirect() {
    LauncherPane(
        modifier = Modifier,
        windowSizeClass = WindowSizeClass.calculateFromSize(
            size = DpSize(1280.dp, 800.dp)
        )
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedPreview
@Composable
private fun PreviewTabletPortraitDirect() {
    LauncherPane(
        modifier = Modifier,
        windowSizeClass = WindowSizeClass.calculateFromSize(
            size = DpSize(1280.dp, 800.dp)
        )
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumPreview
@Composable
private fun PreviewPhonePortraitDirect() {
    LauncherPane(
        modifier = Modifier,
        windowSizeClass = WindowSizeClass.calculateFromSize(
            size = DpSize(600.dp, 900.dp)
        )
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhoneLandscapePreview
@Composable
private fun PreviewPhoneLandscapeDirect() {
    LauncherPane(
        modifier = Modifier,
        windowSizeClass = WindowSizeClass.calculateFromSize(
            size = DpSize(780.dp, 360.dp)
        )
    )
}
