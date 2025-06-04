package com.dhimandasgupta.notemark.ui.screens

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun LauncherPane(
    modifier: Modifier = Modifier
) {
    val windowSizeClass = calculateWindowSizeClass(LocalActivity.current as Activity)

    when(windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            LandingPanePortrait()
        }
        WindowWidthSizeClass.Medium -> {
            LandingPaneLandscape()
        }
        WindowWidthSizeClass.Expanded -> {
            LandingPaneTablet()
        }
        else -> {
            LandingPanePortrait()
        }
    }
}

@Preview
@Composable
private fun LandingPanePortrait(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colorScheme.background),
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = modifier.fillMaxSize()
            )
        }

        Column(
            modifier = modifier
                .offset(
                    y = (-16).dp
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp
                    )
                )
                .fillMaxSize()
                .background(colorScheme.background)
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

            Button(
                onClick = {},
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Get Started")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {},
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Log in")
            }

            Spacer(Modifier.fillMaxSize())
        }
    }
}

@Preview(
    widthDp = 640,
    heightDp = 360
)
@Composable
private fun LandingPaneLandscape(modifier: Modifier = Modifier) {
    Row {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .fillMaxWidth(0.45f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = modifier.fillMaxSize()
            )
        }

        Box(
            modifier = modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(colorResource(R.color.splash_blue))
                .padding(start = 16.dp, top = 32.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            bottomStart = 16.dp
                        )
                    )
                    .fillMaxSize()
                    .background(colorScheme.background)
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

                Button(
                    onClick = {},
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = "Get Started")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {},
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = "Log in")
                }

                Spacer(Modifier.fillMaxSize())
            }
        }
    }
}

@Preview(
    name = "Tablet Landscape",
    showBackground = true,
    device = Devices.TABLET // Or Devices.NEXUS_10, etc.
)
@Composable
private fun LandingPaneTablet(modifier: Modifier = Modifier) {
    Column {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = modifier.fillMaxSize()
            )
        }

        Box(
            modifier = modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(colorResource(R.color.splash_blue))
                .padding(start = 16.dp, top = 16.dp, bottom = 0.dp, end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp
                        )
                    )
                    .fillMaxSize()
                    .background(colorScheme.background)
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

                Button(
                    onClick = {},
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = "Get Started")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {},
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = "Log in")
                }

                Spacer(Modifier.fillMaxSize())
            }
        }
    }
}
