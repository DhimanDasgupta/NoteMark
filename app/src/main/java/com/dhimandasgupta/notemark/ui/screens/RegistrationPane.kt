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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
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
            value = registrationState.userName,
            onValueChange = { registrationAction(UserNameEntered(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.hasFocus) registrationAction(UserNameFocusChanged)
                },
            maxLines = 1,
            visualTransformation = VisualTransformation.None,
            placeholder = { Text("Enter your user name here") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Unspecified,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            )
        )

        registrationState.userNameError?.let { error ->
            Text(text = error, style = typography.labelSmall, color = colorScheme.error)
        }

        Text(
            text = "Email",
            style = typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = registrationState.email,
            onValueChange = { registrationAction(EmailEntered(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.hasFocus) registrationAction(EmailFocusChanged)
                },
            maxLines = 1,
            visualTransformation = VisualTransformation.None,
            placeholder = { Text("Enter your email here") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            )
        )

        registrationState.emailError?.let { error ->
            Text(text = error, style = typography.labelSmall, color = colorScheme.error)
        }

        Text(
            text = "Password",
            style = typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = registrationState.password,
            onValueChange = { registrationAction(PasswordEntered(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.hasFocus) registrationAction(PasswordFocusChanged)
                },
            maxLines = 1,
            visualTransformation = PasswordVisualTransformation(),
            placeholder = { Text("Enter your password here") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            )
        )

        registrationState.passwordError?.let { error ->
            Text(text = error, style = typography.labelSmall, color = colorScheme.error)
        }

        Text(
            text = "Repeat password",
            style = typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = registrationState.repeatPassword,
            onValueChange = { registrationAction(RepeatPasswordEntered(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.hasFocus) registrationAction(RepeatPasswordFocusChanged)
                },
            maxLines = 1,
            visualTransformation = PasswordVisualTransformation(),
            placeholder = { Text("Retype your password here") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(true) }
            )
        )

        registrationState.repeatPasswordError?.let { error ->
            Text(text = error, style = typography.labelSmall, color = colorScheme.error)
        }

        NoteMarkButton(
            onClick = {},
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