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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.statemachine.LoginAction
import com.dhimandasgupta.notemark.statemachine.LoginAction.EmailEntered
import com.dhimandasgupta.notemark.statemachine.LoginAction.EmailFocusChanged
import com.dhimandasgupta.notemark.statemachine.LoginAction.HideLoginButton
import com.dhimandasgupta.notemark.statemachine.LoginAction.LoginClicked
import com.dhimandasgupta.notemark.statemachine.LoginAction.PasswordEntered
import com.dhimandasgupta.notemark.statemachine.LoginAction.PasswordFocusChanged
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
                        .background(colorScheme.background)
                        .fillMaxSize()
                        .padding(all = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        .background(colorScheme.background)
                        .fillMaxSize()
                        .padding(start = 64.dp, end = 64.dp, top = 64.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LeftPane()
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
                        .background(colorScheme.background)
                        .fillMaxSize()
                        .padding(all = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LeftPane()
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
        Text(
            text = "Email",
            style = typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = loginState.email,
            onValueChange = { loginAction(EmailEntered(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.hasFocus) loginAction(EmailFocusChanged)
                },
            visualTransformation = VisualTransformation.None,
            placeholder = { Text("Enter your email here") },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Unspecified,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        loginState.emailError?.let { error ->
            Text(text = error, style = typography.labelSmall, color = colorScheme.error)
        }

        Text(
            text = "Password",
            style = typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = loginState.password,
            onValueChange = { loginAction(PasswordEntered(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.hasFocus) {
                        loginAction(PasswordFocusChanged)
                    }
                },
            visualTransformation = PasswordVisualTransformation(),
            placeholder = { Text("Enter your Password here") },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Unspecified,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (loginState.loginEnabled) {
                        loginAction(HideLoginButton)
                        loginAction(LoginClicked)
                    }
                    focusManager.moveFocus(FocusDirection.Down)
                    keyboardController?.hide()
                    focusManager.clearFocus(true)
                }
            )
        )

        loginState.passwordError?.let { error ->
            Text(text = error, style = typography.labelSmall, color = colorScheme.error)
        }

        NoteMarkButton(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus(true)
                loginAction(HideLoginButton)
                loginAction(LoginClicked)
            },
            modifier = modifier.fillMaxWidth(),
            enabled = loginState.loginEnabled
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

        Spacer(modifier = Modifier.imePadding())
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhonePortraitPreview
@Composable
private fun PhonePortraitPreview() {
    LoginPane(
        modifier = Modifier,
        windowSizeClass = phonePortrait,
        loginState = LoginState()
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhoneLandscapePreview
@Composable
private fun PhoneLandscapePreview() {
    LoginPane(
        modifier = Modifier,
        windowSizeClass = phoneLandscape,
        loginState = LoginState()
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumPortraitPreview
@Composable
private fun TabletMediumPortraitPreview() {
    LoginPane(
        modifier = Modifier,
        windowSizeClass = mediumTabletPortrait,
        loginState = LoginState()
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumLandscapePreview
@Composable
private fun TabletMediumLandscapePreview() {
    LoginPane(
        modifier = Modifier,
        windowSizeClass = mediumTabletLandscape,
        loginState = LoginState()
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedPortraitPreview
@Composable
private fun TabletExpandedPortraitPreview() {
    LoginPane(
        modifier = Modifier,
        windowSizeClass = extendedTabletPortrait,
        loginState = LoginState()
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedLandscapePreview
@Composable
private fun TabletExpandedLandscapePreview() {
    LoginPane(
        modifier = Modifier,
        windowSizeClass = extendedTabletLandscape,
        loginState = LoginState()
    )
}