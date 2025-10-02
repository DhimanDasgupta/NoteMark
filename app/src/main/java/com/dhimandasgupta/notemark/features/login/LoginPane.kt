package com.dhimandasgupta.notemark.features.login

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.dhimandasgupta.notemark.features.login.LoginAction.EmailEntered
import com.dhimandasgupta.notemark.features.login.LoginAction.HideLoginButton
import com.dhimandasgupta.notemark.features.login.LoginAction.LoginClicked
import com.dhimandasgupta.notemark.features.login.LoginAction.PasswordEntered
import com.dhimandasgupta.notemark.ui.PhoneLandscapePreview
import com.dhimandasgupta.notemark.ui.PhonePortraitPreview
import com.dhimandasgupta.notemark.ui.TabletExpandedLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletExpandedPortraitPreview
import com.dhimandasgupta.notemark.ui.TabletMediumLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletMediumPortraitPreview
import com.dhimandasgupta.notemark.ui.common.DeviceLayoutType
import com.dhimandasgupta.notemark.ui.common.alignToSafeDrawing
import com.dhimandasgupta.notemark.ui.common.getDeviceLayoutType
import com.dhimandasgupta.notemark.ui.common.lifecycleAwareDebouncedClickable
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun LoginPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    navigateToAfterLogin: () -> Unit = {},
    navigateToRegistration: () -> Unit = {},
    loginUiModel: LoginUiModel,
    loginAction: (LoginAction) -> Unit = {},
) {
    val updatedLoginUiModel by rememberUpdatedState(newValue = loginUiModel)

    Box(
        modifier = modifier
            .background(color = colorResource(id = R.color.splash_blue))
            .fillMaxSize()
    ) {
        val layoutType = getDeviceLayoutType(windowSizeClass)

        when (layoutType) {
            DeviceLayoutType.PHONE_LANDSCAPE -> {
                Row(
                    modifier = Modifier
                        .padding(
                            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                        )
                        .clip(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp
                            )
                        )
                        .background(color = colorScheme.surfaceContainerLowest)
                        .fillMaxSize()
                        .padding(all = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    LeftPane(
                        modifier = Modifier
                            .safeContentPadding()
                            .fillMaxWidth(fraction = 0.4f)
                    )
                    RightPane(
                        modifier = Modifier
                            .padding(
                                top = WindowInsets.systemBars.asPaddingValues()
                                    .calculateTopPadding(),
                                start = WindowInsets
                                    .systemBars.union(insets = WindowInsets.displayCutout)
                                    .asPaddingValues()
                                    .calculateLeftPadding(LayoutDirection.Ltr),
                                end = WindowInsets
                                    .systemBars.union(insets = WindowInsets.displayCutout)
                                    .asPaddingValues()
                                    .calculateRightPadding(LayoutDirection.Ltr)
                            )
                            .verticalScroll(state = rememberScrollState()),
                        navigateToRegistration = navigateToRegistration,
                        navigateToAfterLogin = navigateToAfterLogin,
                        loginUiModel = updatedLoginUiModel,
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
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp
                            )
                        )
                        .background(color = colorScheme.surfaceContainerLowest)
                        .fillMaxSize()
                        .padding(start = 64.dp, end = 64.dp, top = 64.dp)
                        .verticalScroll(state = rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    LeftPane(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    Spacer(modifier = Modifier.height(height = 16.dp))
                    RightPane(
                        navigateToRegistration = navigateToRegistration,
                        navigateToAfterLogin = navigateToAfterLogin,
                        loginUiModel = loginUiModel,
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
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp
                            )
                        )
                        .background(color = colorScheme.surfaceContainerLowest)
                        .fillMaxSize()
                        .padding(all = 16.dp)
                        .verticalScroll(state = rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    LeftPane()
                    Spacer(modifier = Modifier.height(height = 16.dp))
                    RightPane(
                        navigateToRegistration = navigateToRegistration,
                        navigateToAfterLogin = navigateToAfterLogin,
                        loginUiModel = updatedLoginUiModel,
                        loginAction = loginAction
                    )
                }
            }
        }
    }
}

@Composable
private fun LeftPane(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        horizontalAlignment = horizontalAlignment
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
    loginUiModel: LoginUiModel,
    loginAction: (LoginAction) -> Unit = {},
    navigateToAfterLogin: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalActivity.current

    LaunchedEffect(key1 = Unit) { focusManager.clearFocus() }

    LaunchedEffect(key1 = loginUiModel.loginSuccess) {
        if (loginUiModel.loginSuccess == null) return@LaunchedEffect
        Toast.makeText(
            context,
            if (loginUiModel.loginSuccess) "Login successful" else "Login failed",
            Toast.LENGTH_SHORT
        ).show()
        loginAction(LoginAction.LoginChangeConsumed)
        if (loginUiModel.loginSuccess) {
            navigateToAfterLogin()
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        var emailText by rememberSaveable { mutableStateOf(value = loginUiModel.email) }
        LaunchedEffect(key1 = emailText) {
            snapshotFlow { emailText }.collect {
                delay(timeMillis = 50)
                loginAction(EmailEntered(email = emailText))
            }
        }

        NoteMarkTextField(
            modifier = Modifier
                .fillMaxWidth()
                .alignToSafeDrawing(),
            label = "Email",
            enteredText = emailText,
            hintText = "john.doe@gmail.com",
            onTextChanged = { emailText = it },
            onNextClicked = { focusManager.moveFocus(FocusDirection.Next) }
        )

        var passwordText by rememberSaveable { mutableStateOf(value = loginUiModel.password) }
        LaunchedEffect(key1 = passwordText) {
            snapshotFlow { passwordText }.collect {
                delay(timeMillis = 50)
                loginAction(PasswordEntered(password = passwordText))
            }
        }

        NoteMarkPasswordTextField(
            modifier = Modifier
                .fillMaxWidth()
                .alignToSafeDrawing(),
            label = "Password",
            enteredText = passwordText,
            hintText = "Password",
            onTextChanged = { passwordText = it },
            onDoneClicked = {
                focusManager.moveFocus(FocusDirection.Enter)
                keyboardController?.hide()
                focusManager.clearFocus(force = true)

                if (loginUiModel.loginEnabled) {
                    loginAction(HideLoginButton)
                    loginAction(LoginClicked)
                }
            }
        )

        NoteMarkButton(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus(force = true)
                loginAction(HideLoginButton)
                loginAction(LoginClicked)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = loginUiModel.loginEnabled
        ) {
            Text(
                text = "Log in",
                style = typography.titleSmall
            )
        }

        Text(
            text = "Don't have an account?",
            style = typography.titleSmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .fillMaxSize()
                .lifecycleAwareDebouncedClickable {
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
            loginUiModel = LoginUiModel.Empty
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
            loginUiModel = LoginUiModel.Empty
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
            loginUiModel = LoginUiModel.Empty
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
            loginUiModel = LoginUiModel.Empty
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
            loginUiModel = LoginUiModel.Empty
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
            loginUiModel = LoginUiModel.Empty
        )
    }
}