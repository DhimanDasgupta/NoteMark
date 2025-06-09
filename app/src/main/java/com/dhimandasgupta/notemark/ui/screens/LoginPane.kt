package com.dhimandasgupta.notemark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun LoginPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    navigateToAfterLogin: () -> Unit = {},
    navigateToRegistration: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .background(color = colorResource(R.color.splash_blue))
            .fillMaxSize()
    ) {
        val layoutType = getDeviceLayoutType(windowSizeClass)

        when(layoutType) {
            DeviceLayoutType.PHONE_LANDSCAPE -> {
                Row(
                    modifier = Modifier
                        .padding(
                            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                            start = WindowInsets
                                .systemBars.union(WindowInsets.displayCutout)
                                .asPaddingValues()
                                .calculateLeftPadding(LayoutDirection.Ltr),
                            end = WindowInsets
                                .systemBars.union(WindowInsets.displayCutout)
                                .asPaddingValues()
                                .calculateRightPadding(LayoutDirection.Ltr)
                        )
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp
                            )
                        )
                        .background(colorScheme.background)
                        .fillMaxSize()
                        .padding(all = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LeftPane(
                        modifier = Modifier
                            .safeContentPadding()
                            .fillMaxWidth(0.5f)
                    )
                    RightPane(
                        modifier = Modifier
                            .padding(
                                top = WindowInsets.systemBars.asPaddingValues()
                                    .calculateTopPadding(),
                                start = WindowInsets.systemBars.asPaddingValues()
                                    .calculateLeftPadding(LayoutDirection.Ltr),
                                end = WindowInsets.systemBars.asPaddingValues()
                                    .calculateRightPadding(LayoutDirection.Ltr)
                            ),
                        navigateToRegistration = navigateToRegistration
                    )
                }
            }
            DeviceLayoutType.TABLET_LAYOUT -> {
                Column(
                    modifier = Modifier
                        .padding(
                            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                            start = WindowInsets.systemBars.asPaddingValues()
                                .calculateLeftPadding(LayoutDirection.Ltr),
                            end = WindowInsets.systemBars.asPaddingValues()
                                .calculateRightPadding(LayoutDirection.Ltr)
                        )
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp
                            )
                        )
                        .background(colorScheme.background)
                        .fillMaxSize()
                        .padding(start = 64.dp, end = 64.dp, top = 64.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LeftPane()
                    RightPane(
                        navigateToRegistration = navigateToRegistration
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(
                            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                            start = WindowInsets.systemBars.asPaddingValues()
                                .calculateLeftPadding(LayoutDirection.Ltr),
                            end = WindowInsets.systemBars.asPaddingValues()
                                .calculateRightPadding(LayoutDirection.Ltr)
                        )
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp
                            )
                        )
                        .background(colorScheme.background)
                        .fillMaxSize()
                        .padding(all = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LeftPane()
                    RightPane(
                        navigateToRegistration = navigateToRegistration
                    )
                }
            }
        }
    }
}

@Composable
private fun LeftPane(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Log In",
            style = typography.headlineLarge
        )

        Text(
            text = "Capture your thoughts and ideas",
            style = typography.bodySmall,
            color = typography.headlineLarge.color.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun RightPane(
    modifier: Modifier = Modifier,
    navigateToRegistration: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Email",
            style = typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Unspecified,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            )
        )

        Text(
            text = "Password",
            style = typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Unspecified,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.clearFocus(true) }
            )
        )

        NoteMarkButton(
            onClick = {},
            modifier = modifier.fillMaxWidth()
        ) {
            Text(text = "Log In")
        }

        Text(
            text = "Don't have an account?",
            style = typography.bodySmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    navigateToRegistration()
                },
            textAlign = TextAlign.Center,
            color = colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Tablet Landscape", showSystemUi = true)
@Composable
private fun PreviewTabletLandscapeDirect() {
    LoginPane(
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
    LoginPane(
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
    LoginPane(
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
    LoginPane(
        modifier = Modifier,
        windowSizeClass = WindowSizeClass.calculateFromSize(
            size = DpSize(780.dp, 360.dp)
        )
    )
}