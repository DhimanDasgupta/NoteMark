package com.dhimandasgupta.notemark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

@Composable
fun RegistrationPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    navigateToLogin: () -> Unit = {}
) {
    Box(
        modifier = modifier
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
                        .padding(all = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                        navigateToLogin = navigateToLogin
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
                        .padding(start = 128.dp, end = 128.dp, top = 128.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LeftPane()
                    RightPane(
                        modifier = Modifier.fillMaxWidth(),
                        navigateToLogin = navigateToLogin
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
                        navigateToLogin = navigateToLogin
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
            text = "Create account",
            style = typography.headlineLarge
        )

        Text(
            text = "Capture your thoughts and ideas",
            style = typography.bodySmall,
            color = typography.bodySmall.color.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun RightPane(
    modifier: Modifier = Modifier,
    navigateToLogin: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Username",
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
            text = "Email",
            style = typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
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
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            )
        )

        Text(
            text = "Repeat password",
            style = typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(true) }
            )
        )

        NoteMarkButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(text = "Create account")
        }

        Text(
            text = "Already have and account?",
            style = typography.bodySmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxSize().clickable {
                navigateToLogin()
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
    RegistrationPane(
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
    RegistrationPane(
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
    RegistrationPane(
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
    RegistrationPane(
        modifier = Modifier,
        windowSizeClass = WindowSizeClass.calculateFromSize(
            size = DpSize(780.dp, 360.dp)
        )
    )
}