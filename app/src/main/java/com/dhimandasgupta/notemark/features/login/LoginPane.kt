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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun LoginPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    loginUiModel: () -> LoginUiModel,
    loginAction: (LoginAction) -> Unit = {},
    navigateToAfterLogin: () -> Unit = {},
    navigateToRegistration: () -> Unit = {},
) {
    val updatedLoginUiModel by rememberUpdatedState(newValue = loginUiModel)

    val context = LocalActivity.current

    LaunchedEffect(key1 = Unit) {
        snapshotFlow { loginUiModel().loginSuccess }
            .filter { it != null }
            .collect { isSuccess ->
                val message = if (isSuccess == true) "Login successful" else "Login failed"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                // Always call the latest lambdas
                loginAction(LoginAction.LoginChangeConsumed)
                if (isSuccess == true) {
                    navigateToAfterLogin()
                }
            }
    }

    Box(
        modifier = modifier
            .background(color = colorResource(id = R.color.splash_blue))
            .fillMaxSize()
    ) {
        val layoutType = getDeviceLayoutType(windowSizeClass)

        when (layoutType) {
            DeviceLayoutType.PHONE_PORTRAIT -> PhonePortraitLayout(
                modifier = Modifier,
                loginUiModel = updatedLoginUiModel,
                loginAction = loginAction,
                navigateToRegistration = navigateToRegistration
            )
            DeviceLayoutType.PHONE_LANDSCAPE -> PhoneLandscapeLayout(
                modifier = Modifier,
                loginUiModel = updatedLoginUiModel,
                loginAction = loginAction,
                navigateToRegistration = navigateToRegistration
            )
            else -> TabletLayout(
                modifier = Modifier,
                loginUiModel = updatedLoginUiModel,
                loginAction = loginAction,
                navigateToRegistration = navigateToRegistration
            )
        }
    }
}

@Composable
private fun PhoneLandscapeLayout(
    modifier: Modifier = Modifier,
    loginUiModel: () -> LoginUiModel,
    loginAction: (LoginAction) -> Unit = {},
    navigateToRegistration: () -> Unit = {},
) {
    Row(
        modifier = modifier
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
                .align(Alignment.CenterVertically)
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
            loginUiModel = loginUiModel,
            loginAction = loginAction
        )
    }
}

@Composable
private fun TabletLayout(
    modifier: Modifier = Modifier,
    loginUiModel: () -> LoginUiModel,
    loginAction: (LoginAction) -> Unit = {},
    navigateToRegistration: () -> Unit = {},
) {
    Column(
        modifier = modifier
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
            loginUiModel = loginUiModel,
            loginAction = loginAction
        )
    }
}

@Composable
private fun PhonePortraitLayout(
    modifier: Modifier = Modifier,
    loginUiModel: () -> LoginUiModel,
    loginAction: (LoginAction) -> Unit = {},
    navigateToRegistration: () -> Unit = {},
) {
    Column(
        modifier = modifier
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
            loginUiModel = loginUiModel,
            loginAction = loginAction
        )
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
    loginUiModel: () -> LoginUiModel,
    navigateToRegistration: () -> Unit = {},
    loginAction: (LoginAction) -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = Unit) { focusManager.clearFocus() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        LoginEmailField(
            modifier = Modifier,
            loginUiModel = loginUiModel,
            loginAction = loginAction,
            focusManager = focusManager
        )

        LoginPasswordField(
            modifier = Modifier,
            loginUiModel = loginUiModel,
            loginAction = loginAction,
            keyboardController = keyboardController,
            focusManager = focusManager
        )

        LoginButton(
            modifier = Modifier,
            loginUiModel = loginUiModel,
            loginAction = loginAction,
            keyboardController = keyboardController,
            focusManager = focusManager
        )

        LoginFooterField(
            modifier = Modifier,
            navigateToRegistration = navigateToRegistration
        )

        Spacer(modifier = Modifier.imePadding())
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun LoginEmailField(
    modifier: Modifier = Modifier,
    loginUiModel: () -> LoginUiModel,
    loginAction: (LoginAction) -> Unit = {},
    focusManager: FocusManager,
) {
    var email by remember { mutableStateOf(value = loginUiModel().email) }
    LaunchedEffect(key1 = Unit) {
        snapshotFlow { email }
            .debounce(timeoutMillis = 200)
            .collect { loginAction(EmailEntered(email = email)) }
    }

    NoteMarkTextField(
        modifier = modifier
            .fillMaxWidth()
            .alignToSafeDrawing(),
        label = "Email",
        enteredText = email,
        hintText = "john.doe@gmail.com",
        onTextChanged = { email = it },
        onNextClicked = { focusManager.moveFocus(FocusDirection.Next) }
    )
}

@OptIn(FlowPreview::class)
@Composable
private fun LoginPasswordField(
    modifier: Modifier = Modifier,
    loginUiModel: () -> LoginUiModel,
    loginAction: (LoginAction) -> Unit = {},
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
) {
    var password by remember { mutableStateOf(value = loginUiModel().password) }
    LaunchedEffect(key1 = Unit) {
        snapshotFlow { password }
            .debounce(timeoutMillis = 200)
            .collect { loginAction(PasswordEntered(password = password)) }
    }

    NoteMarkPasswordTextField(
        modifier = modifier
            .fillMaxWidth()
            .alignToSafeDrawing(),
        label = "Password",
        enteredText = password,
        hintText = "Password",
        onTextChanged = { password = it },
        onDoneClicked = {
            focusManager.moveFocus(FocusDirection.Enter)
            keyboardController?.hide()
            focusManager.clearFocus(force = true)

            if (loginUiModel().loginEnabled) {
                loginAction(HideLoginButton)
                loginAction(LoginClicked)
            }
        }
    )
}

@Composable
private fun LoginButton(
    modifier: Modifier = Modifier,
    loginUiModel: () -> LoginUiModel,
    loginAction: (LoginAction) -> Unit = {},
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
) {
    NoteMarkButton(
        onClick = {
            keyboardController?.hide()
            focusManager.clearFocus(force = true)
            loginAction(HideLoginButton)
            loginAction(LoginClicked)
        },
        modifier = modifier.fillMaxWidth(),
        enabled = loginUiModel().loginEnabled
    ) {
        Text(
            text = "Log in",
            style = typography.titleSmall
        )
    }
}

@Composable
private fun LoginFooterField(
    modifier: Modifier = Modifier,
    navigateToRegistration: () -> Unit = {},
) {
    Text(
        text = "Don't have an account?",
        style = typography.titleSmall,
        fontWeight = FontWeight.Normal,
        modifier = modifier
            .fillMaxSize()
            .lifecycleAwareDebouncedClickable {
                navigateToRegistration()
            },
        textAlign = TextAlign.Center,
        color = colorScheme.primary
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhonePortraitPreview
@Composable
private fun PhonePortraitPreview() {
    NoteMarkTheme {
        LoginPane(
            modifier = Modifier,
            windowSizeClass = phonePortrait,
            loginUiModel = { LoginUiModel.Empty }
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
            loginUiModel = { LoginUiModel.Empty }
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
            loginUiModel = { LoginUiModel.Empty }
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
            loginUiModel = { LoginUiModel.Empty }
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
            loginUiModel = { LoginUiModel.Empty }
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
            loginUiModel = { LoginUiModel.Empty }
        )
    }
}