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
import com.dhimandasgupta.notemark.statemachine.LoginAction
import com.dhimandasgupta.notemark.statemachine.LoginAction.EmailEntered
import com.dhimandasgupta.notemark.statemachine.LoginAction.HideLoginButton
import com.dhimandasgupta.notemark.statemachine.LoginAction.LoginClicked
import com.dhimandasgupta.notemark.statemachine.LoginAction.PasswordEntered
import com.dhimandasgupta.notemark.statemachine.LoginState
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
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import com.dhimandasgupta.notemark.ui.extendedTabletLandscape
import com.dhimandasgupta.notemark.ui.extendedTabletPortrait
import com.dhimandasgupta.notemark.ui.mediumTabletLandscape
import com.dhimandasgupta.notemark.ui.mediumTabletPortrait
import com.dhimandasgupta.notemark.ui.phoneLandscape
import com.dhimandasgupta.notemark.ui.phonePortrait

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun LoginPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    navigateToAfterLogin: () -> Unit = {},
    navigateToRegistration: () -> Unit = {},
    loginState: LoginState,
    loginAction: (LoginAction) -> Unit = {},
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
                        .background(colorScheme.surfaceContainerLowest)
                        .fillMaxSize()
                        .padding(all = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    LeftPane(
                        modifier = Modifier
                            .safeContentPadding()
                            .fillMaxWidth(0.4f)
                    )
                    RightPane(
                        modifier =Modifier
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
                            )
                            .verticalScroll(rememberScrollState()),
                        navigateToRegistration = navigateToRegistration,
                        navigateToAfterLogin = navigateToAfterLogin,
                        loginState = loginState,
                        loginAction = loginAction
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
                        .background(colorScheme.surfaceContainerLowest)
                        .fillMaxSize()
                        .padding(start = 64.dp, end = 64.dp, top = 64.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LeftPane()
                    Spacer(modifier = Modifier.height(16.dp))
                    RightPane(
                        navigateToRegistration = navigateToRegistration,
                        navigateToAfterLogin = navigateToAfterLogin,
                        loginState = loginState,
                        loginAction = loginAction
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
                        .background(colorScheme.surfaceContainerLowest)
                        .fillMaxSize()
                        .padding(all = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LeftPane()
                    Spacer(modifier = Modifier.height(16.dp))
                    RightPane(
                        navigateToRegistration = navigateToRegistration,
                        navigateToAfterLogin = navigateToAfterLogin,
                        loginState = loginState,
                        loginAction = loginAction
                    )
                }
            }
        }
    }
}

@Composable
private fun LeftPane(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Log In",
            style = typography.titleLarge
        )

        Text(
            text = "Capture your thoughts and ideas",
            style = typography.bodyLarge,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RightPane(
    modifier: Modifier = Modifier,
    navigateToRegistration: () -> Unit = {},
    loginState: LoginState,
    loginAction: (LoginAction) -> Unit = {},
    navigateToAfterLogin: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalActivity.current

    LaunchedEffect(Unit) { focusManager.clearFocus() }

    LaunchedEffect(loginState.loginSuccess) {
        if (loginState.loginSuccess == null) return@LaunchedEffect
        Toast.makeText(context, if (loginState.loginSuccess == true) "Login successful" else "Login failed", Toast.LENGTH_SHORT).show()
        loginAction(LoginAction.LoginChangeConsumed)
        if (loginState.loginSuccess == true) {
            navigateToAfterLogin()
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NoteMarkTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "Email",
            enteredText = loginState.email,
            hintText = "Enter your email here",
            explanationText = loginState.emailError ?: "",
            onTextChanged = { loginAction(EmailEntered(it)) },
            onNextClicked = { focusManager.moveFocus(FocusDirection.Next) }
        )

        NoteMarkPasswordTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "Password",
            enteredText = loginState.password,
            hintText = "Enter your Password here",
            explanationText = loginState.passwordError ?: "",
            onTextChanged = { loginAction(PasswordEntered(it)) },
            onDoneClicked = {
                if (loginState.loginEnabled) {
                    loginAction(HideLoginButton)
                    loginAction(LoginClicked)
                }
                focusManager.moveFocus(FocusDirection.Enter)
                keyboardController?.hide()
                focusManager.clearFocus(true) }
        )

        NoteMarkButton(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus(true)
                loginAction(HideLoginButton)
                loginAction(LoginClicked)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = loginState.loginEnabled
        ) {
            Text(
                text = "Log In",
                style = typography.titleSmall
            )
        }

        Text(
            text = "Don't have an account?",
            style = typography.titleSmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    navigateToRegistration()
                },
            textAlign = TextAlign.Center,
            color = colorScheme.primary
        )

        Spacer(modifier = Modifier.imePadding())
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhonePortraitPreview
@Composable
private fun PhonePortraitPreview() {
    NoteMarkTheme {
        LoginPane(
            modifier = Modifier,
            windowSizeClass = phonePortrait,
            loginState = LoginState()
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhoneLandscapePreview
@Composable
private fun PhoneLandscapePreview() {
    NoteMarkTheme {
        LoginPane(
            modifier = Modifier,
            windowSizeClass = phoneLandscape,
            loginState = LoginState()
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumPortraitPreview
@Composable
private fun TabletMediumPortraitPreview() {
    NoteMarkTheme {
        LoginPane(
            modifier = Modifier,
            windowSizeClass = mediumTabletPortrait,
            loginState = LoginState()
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumLandscapePreview
@Composable
private fun TabletMediumLandscapePreview() {
    NoteMarkTheme {
        LoginPane(
            modifier = Modifier,
            windowSizeClass = mediumTabletLandscape,
            loginState = LoginState()
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedPortraitPreview
@Composable
private fun TabletExpandedPortraitPreview() {
    NoteMarkTheme {
        LoginPane(
            modifier = Modifier,
            windowSizeClass = extendedTabletPortrait,
            loginState = LoginState()
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedLandscapePreview
@Composable
private fun TabletExpandedLandscapePreview() {
    NoteMarkTheme {
        LoginPane(
            modifier = Modifier,
            windowSizeClass = extendedTabletLandscape,
            loginState = LoginState()
        )
    }
}