package com.dhimandasgupta.notemark.ui.screens

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.statemachine.RegistrationAction
import com.dhimandasgupta.notemark.statemachine.RegistrationAction.*
import com.dhimandasgupta.notemark.statemachine.RegistrationState
import com.dhimandasgupta.notemark.ui.PhoneLandscapePreview
import com.dhimandasgupta.notemark.ui.PhonePortraitPreview
import com.dhimandasgupta.notemark.ui.TabletExpandedLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletExpandedPortraitPreview
import com.dhimandasgupta.notemark.ui.TabletMediumLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletMediumPortraitPreview
import com.dhimandasgupta.notemark.ui.common.DeviceLayoutType
import com.dhimandasgupta.notemark.ui.common.getDeviceLayoutType
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkButton
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkPasswordTextField
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTextField
import com.dhimandasgupta.notemark.ui.extendedTabletLandscape
import com.dhimandasgupta.notemark.ui.extendedTabletPortrait
import com.dhimandasgupta.notemark.ui.mediumTabletLandscape
import com.dhimandasgupta.notemark.ui.mediumTabletPortrait
import com.dhimandasgupta.notemark.ui.phoneLandscape
import com.dhimandasgupta.notemark.ui.phonePortrait

@Composable
fun RegistrationPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    navigateToLogin: () -> Unit = {},
    registrationState: RegistrationState,
    registrationAction: (RegistrationAction) -> Unit = {}
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
                            .fillMaxWidth(0.4f)
                    )
                    RightPane(
                        modifier = Modifier
                            .padding(
                                top = WindowInsets.systemBars.asPaddingValues()
                                    .calculateTopPadding(),
                                start = WindowInsets
                                    .systemBars.union(WindowInsets.displayCutout)
                                    .asPaddingValues()
                                    .calculateLeftPadding(LayoutDirection.Ltr),
                                end = WindowInsets
                                    .systemBars.union(WindowInsets.displayCutout)
                                    .asPaddingValues()
                                    .calculateRightPadding(LayoutDirection.Ltr)
                            ),
                        navigateToLogin = navigateToLogin,
                        registrationState = registrationState,
                        registrationAction = registrationAction
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
                        navigateToLogin = navigateToLogin,
                        registrationState = registrationState,
                        registrationAction = registrationAction
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
                        navigateToLogin = navigateToLogin,
                        registrationState = registrationState,
                        registrationAction = registrationAction
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
    navigateToLogin: () -> Unit = {},
    registrationState: RegistrationState,
    registrationAction: (RegistrationAction) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalActivity.current

    LaunchedEffect(Unit) { focusManager.clearFocus() }

    LaunchedEffect(registrationState.registrationSuccess) {
        if (registrationState.registrationSuccess == null) return@LaunchedEffect
        registrationAction(RegistrationChangeStatusConsumed)
        Toast.makeText(context, if (registrationState.registrationSuccess == true) "Registration successful" else "Registration failed", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NoteMarkTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "Username",
            enteredText = registrationState.userName,
            hintText = "Enter your user name here",
            explanationText = "Please enter valid username",
            showExplanationText = true,
            onTextChanged = { registrationAction(UserNameEntered(it)) },
            onFocusGained = { registrationAction(UserNameFocusChanged) },
            onNextClicked = { focusManager.moveFocus(FocusDirection.Next) }
        )

        NoteMarkTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "Email",
            enteredText = registrationState.email,
            hintText = "Enter your email here",
            explanationText = "Please enter valid email",
            showExplanationText = true,
            onTextChanged = { registrationAction(EmailEntered(it)) },
            onFocusGained = { registrationAction(EmailFocusChanged) },
            onNextClicked = { focusManager.moveFocus(FocusDirection.Next) }
        )

        NoteMarkPasswordTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "Password",
            enteredText = registrationState.password,
            hintText = "Enter your password here",
            explanationText = "Please enter password",
            showExplanationText = true,
            onTextChanged = { registrationAction(PasswordEntered(it)) },
            onFocusGained = { registrationAction(PasswordFocusChanged) },
            onNextClicked = { focusManager.moveFocus(FocusDirection.Next) }
        )

        NoteMarkPasswordTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "Repeat password",
            enteredText = registrationState.password,
            hintText = "Retype your password here",
            explanationText = "Please enter same password here",
            showExplanationText = true,
            onTextChanged = { registrationAction(RepeatPasswordEntered(it)) },
            onFocusGained = { registrationAction(RepeatPasswordFocusChanged) },
            onDoneClicked = {
                if (registrationState.registrationEnabled) {
                    registrationAction(RegisterClicked)
                }
                focusManager.moveFocus(FocusDirection.Exit)
                keyboardController?.hide()
                focusManager.clearFocus(true)
            }
        )

        NoteMarkButton(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus(true)
                registrationAction(RegisterClicked)
            },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = registrationState.registrationEnabled
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

        Spacer(
            modifier = Modifier
                .height(32.dp)
                .imePadding()
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhonePortraitPreview
@Composable
private fun PhonePortraitPreview() {
    RegistrationPane(
        modifier = Modifier,
        windowSizeClass = phonePortrait,
        registrationState = RegistrationState()
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhoneLandscapePreview
@Composable
private fun PhoneLandscapePreview() {
    RegistrationPane(
        modifier = Modifier,
        windowSizeClass = phoneLandscape,
        registrationState = RegistrationState()
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumPortraitPreview
@Composable
private fun TabletMediumPortraitPreview() {
    RegistrationPane(
        modifier = Modifier,
        windowSizeClass = mediumTabletPortrait,
        registrationState = RegistrationState()
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumLandscapePreview
@Composable
private fun TabletMediumLandscapePreview() {
    RegistrationPane(
        modifier = Modifier,
        windowSizeClass = mediumTabletLandscape,
        registrationState = RegistrationState()
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedPortraitPreview
@Composable
private fun TabletExpandedPortraitPreview() {
    RegistrationPane(
        modifier = Modifier,
        windowSizeClass = extendedTabletPortrait,
        registrationState = RegistrationState()
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedLandscapePreview
@Composable
private fun TabletExpandedLandscapePreview() {
    RegistrationPane(
        modifier = Modifier,
        windowSizeClass = extendedTabletLandscape,
        registrationState = RegistrationState()
    )
}