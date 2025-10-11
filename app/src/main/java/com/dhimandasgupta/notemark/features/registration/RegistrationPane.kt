package com.dhimandasgupta.notemark.features.registration

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
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.EmailEntered
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.PasswordEntered
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.PasswordFiledInFocus
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.RegisterClicked
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.RegistrationChangeStatusConsumed
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.RepeatPasswordEntered
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.UserNameEntered
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.UserNameFiledInFocus
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.UserNameFiledLostFocus
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

@Composable
fun RegistrationPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    registrationUiModel: () -> RegistrationUiModel,
    navigateToLogin: () -> Unit = {},
    registrationAction: (RegistrationAction) -> Unit = {}
) {
    val updatedRegistrationUiModel by rememberUpdatedState(newValue = registrationUiModel)

    Box(
        modifier = modifier
            .background(color = colorResource(id = R.color.splash_blue))
            .fillMaxSize()
    ) {
        val layoutType = getDeviceLayoutType(windowSizeClass)

        when (layoutType) {
            DeviceLayoutType.PHONE_PORTRAIT -> PhonePortraitLayout(
                modifier = Modifier,
                registrationUiModel = updatedRegistrationUiModel,
                registrationAction = registrationAction,
                navigateToLogin = navigateToLogin
            )
            DeviceLayoutType.PHONE_LANDSCAPE -> PhoneLandscapeLayout(
                modifier = Modifier,
                registrationUiModel = updatedRegistrationUiModel,
                registrationAction = registrationAction,
                navigateToLogin = navigateToLogin
            )
            else -> TabletLayout(
                modifier = Modifier,
                registrationUiModel = updatedRegistrationUiModel,
                registrationAction = registrationAction,
                navigateToLogin = navigateToLogin
            )
        }
    }
}

@Composable
private fun PhoneLandscapeLayout(
    modifier: Modifier = Modifier,
    registrationUiModel: () -> RegistrationUiModel,
    registrationAction: (RegistrationAction) -> Unit = {},
    navigateToLogin: () -> Unit = {}
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
                .fillMaxSize(fraction = 0.4f)
                .align(alignment = Alignment.CenterVertically)
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
            navigateToLogin = navigateToLogin,
            registrationUiModel = registrationUiModel,
            registrationAction = registrationAction
        )
    }
}

@Composable
private fun TabletLayout(
    modifier: Modifier = Modifier,
    registrationUiModel: () -> RegistrationUiModel,
    registrationAction: (RegistrationAction) -> Unit = {},
    navigateToLogin: () -> Unit = {}
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
            .padding(start = 128.dp, end = 128.dp, top = 128.dp)
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LeftPane()
        Spacer(modifier = Modifier.height(height = 16.dp))
        RightPane(
            modifier = Modifier.fillMaxWidth(),
            navigateToLogin = navigateToLogin,
            registrationUiModel = registrationUiModel,
            registrationAction = registrationAction
        )
    }
}

@Composable
private fun PhonePortraitLayout(
    modifier: Modifier = Modifier,
    registrationUiModel: () -> RegistrationUiModel,
    registrationAction: (RegistrationAction) -> Unit = {},
    navigateToLogin: () -> Unit = {}
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
            navigateToLogin = navigateToLogin,
            registrationUiModel = registrationUiModel,
            registrationAction = registrationAction
        )
    }
}

@Composable
private fun LeftPane(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = "Create account",
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
    navigateToLogin: () -> Unit = {},
    registrationUiModel: () -> RegistrationUiModel,
    registrationAction: (RegistrationAction) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalActivity.current

    LaunchedEffect(key1 = Unit) { focusManager.clearFocus() }

    LaunchedEffect(key1 = registrationUiModel().registrationSuccess) {
        if (registrationUiModel().registrationSuccess == null) return@LaunchedEffect
        registrationAction(RegistrationChangeStatusConsumed)
        Toast.makeText(
            context,
            if (registrationUiModel().registrationSuccess == true) "Registration successful" else "Registration failed",
            Toast.LENGTH_SHORT
        ).show()
        navigateToLogin()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        RegistrationUsernameField(
            modifier = Modifier,
            registrationUiModel = registrationUiModel,
            registrationAction = registrationAction,
            focusManager = focusManager
        )

        RegistrationEmailField(
            modifier = Modifier,
            registrationUiModel = registrationUiModel,
            registrationAction = registrationAction,
            focusManager = focusManager
        )

        RegistrationPasswordField(
            modifier = Modifier,
            registrationUiModel = registrationUiModel,
            registrationAction = registrationAction,
            focusManager = focusManager
        )

        RegistrationRepeatPasswordField(
            modifier = Modifier,
            registrationUiModel = registrationUiModel,
            registrationAction = registrationAction,
            keyboardController = keyboardController,
            focusManager = focusManager
        )

        RegistrationButton(
            modifier = Modifier,
            registrationUiModel = registrationUiModel,
            registrationAction = registrationAction,
            keyboardController = keyboardController,
            focusManager = focusManager
        )

        RegistrationFooterField(
            modifier = Modifier,
            navigateToLogin = navigateToLogin
        )

        Spacer(
            modifier = Modifier
                .height(height = 32.dp)
                .imePadding()
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun RegistrationUsernameField(
    modifier: Modifier = Modifier,
    registrationUiModel: () -> RegistrationUiModel,
    registrationAction: (RegistrationAction) -> Unit = {},
    focusManager: FocusManager,
) {
    var userName by remember { mutableStateOf(value = registrationUiModel().userName) }
    LaunchedEffect(key1 = Unit) {
        snapshotFlow { userName }
            .debounce(timeoutMillis = 300)
            .collect { registrationAction(UserNameEntered(userName)) }
    }

    NoteMarkTextField(
        modifier = modifier
            .fillMaxWidth()
            .alignToSafeDrawing(),
        label = "Username",
        enteredText = userName,
        hintText = "John.doe",
        onFocusGained = { registrationAction(UserNameFiledInFocus(userName = registrationUiModel().userName)) },
        onFocusLost = { registrationAction(UserNameFiledLostFocus(userName = registrationUiModel().userName)) },
        explanationText = registrationUiModel().userNameExplanation ?: "",
        errorText = registrationUiModel().userNameError ?: "",
        onTextChanged = { userName = it },
        onNextClicked = { focusManager.moveFocus(FocusDirection.Next) }
    )
}

@OptIn(FlowPreview::class)
@Composable
private fun RegistrationEmailField(
    modifier: Modifier = Modifier,
    registrationUiModel: () -> RegistrationUiModel,
    registrationAction: (RegistrationAction) -> Unit = {},
    focusManager: FocusManager,
) {
    var email by remember { mutableStateOf(value = registrationUiModel().email) }
    LaunchedEffect(key1 = Unit) {
        snapshotFlow { email }
            .debounce(timeoutMillis = 300)
            .collect { registrationAction(EmailEntered(email)) }
    }

    NoteMarkTextField(
        modifier = modifier
            .fillMaxWidth()
            .alignToSafeDrawing(),
        label = "Email",
        enteredText = email,
        hintText = "john.doe@gmail.com",
        errorText = registrationUiModel().emailError ?: "",
        onTextChanged = { email = it },
        onNextClicked = { focusManager.moveFocus(FocusDirection.Next) }
    )
}

@OptIn(FlowPreview::class)
@Composable
private fun RegistrationPasswordField(
    modifier: Modifier = Modifier,
    registrationUiModel: () -> RegistrationUiModel,
    registrationAction: (RegistrationAction) -> Unit = {},
    focusManager: FocusManager,
) {
    var password by remember { mutableStateOf(registrationUiModel().password) }
    LaunchedEffect(key1 = Unit) {
        snapshotFlow { password }
            .debounce(timeoutMillis = 300)
            .collect { registrationAction(PasswordEntered(password)) }
    }

    NoteMarkPasswordTextField(
        modifier = modifier
            .fillMaxWidth()
            .alignToSafeDrawing(),
        label = "Password",
        enteredText = password,
        hintText = "Password",
        explanationText = registrationUiModel().passwordExplanation ?: "",
        errorText = registrationUiModel().passwordError ?: "",
        onFocusGained = { registrationAction(PasswordFiledInFocus(password = registrationUiModel().password)) },
        onTextChanged = { password = it },
        onNextClicked = { focusManager.moveFocus(FocusDirection.Next) }
    )
}

@OptIn(FlowPreview::class)
@Composable
private fun RegistrationRepeatPasswordField(
    modifier: Modifier = Modifier,
    registrationUiModel: () -> RegistrationUiModel,
    registrationAction: (RegistrationAction) -> Unit = {},
    keyboardController: SoftwareKeyboardController? = null,
    focusManager: FocusManager,
) {
    var repeatPassword by remember { mutableStateOf(value = registrationUiModel().repeatPassword) }
    LaunchedEffect(key1 = Unit) {
        snapshotFlow { repeatPassword }
            .debounce(timeoutMillis = 300)
            .collect { registrationAction(RepeatPasswordEntered(repeatPassword)) }
    }

    NoteMarkPasswordTextField(
        modifier = modifier
            .fillMaxWidth()
            .alignToSafeDrawing(),
        label = "Repeat password",
        enteredText = repeatPassword,
        hintText = "Password",
        errorText = registrationUiModel().repeatPasswordError ?: "",
        onTextChanged = { repeatPassword = it },
        onDoneClicked = {
            if (registrationUiModel().registrationEnabled) {
                registrationAction(RegisterClicked)
            }
            focusManager.moveFocus(FocusDirection.Exit)
            keyboardController?.hide()
            focusManager.clearFocus(force = true)
        }
    )
}

@Composable
private fun RegistrationButton(
    modifier: Modifier = Modifier,
    registrationUiModel: () -> RegistrationUiModel,
    registrationAction: (RegistrationAction) -> Unit = {},
    keyboardController: SoftwareKeyboardController? = null,
    focusManager: FocusManager,
) {
    NoteMarkButton(
        onClick = {
            keyboardController?.hide()
            focusManager.clearFocus(force = true)
            registrationAction(RegisterClicked)
        },
        modifier = modifier
            .fillMaxWidth(),
        enabled = registrationUiModel().registrationEnabled
    ) {
        Text(
            text = "Create account",
            style = typography.titleSmall
        )
    }
}

@Composable
private fun RegistrationFooterField(
    modifier: Modifier = Modifier,
    navigateToLogin: () -> Unit = {}
) {
    Text(
        text = "Already have and account?",
        style = typography.titleSmall,
        fontWeight = FontWeight.Normal,
        modifier = modifier
            .fillMaxSize()
            .lifecycleAwareDebouncedClickable {
                navigateToLogin()
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
        RegistrationPane(
            modifier = Modifier,
            windowSizeClass = phonePortrait,
            registrationUiModel = { RegistrationUiModel.Empty }
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhoneLandscapePreview
@Composable
private fun PhoneLandscapePreview() {
    NoteMarkTheme {
        RegistrationPane(
            modifier = Modifier,
            windowSizeClass = phoneLandscape,
            registrationUiModel = { RegistrationUiModel.Empty }
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumPortraitPreview
@Composable
private fun TabletMediumPortraitPreview() {
    NoteMarkTheme {
        RegistrationPane(
            modifier = Modifier,
            windowSizeClass = mediumTabletPortrait,
            registrationUiModel = { RegistrationUiModel.Empty }
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumLandscapePreview
@Composable
private fun TabletMediumLandscapePreview() {
    NoteMarkTheme {
        RegistrationPane(
            modifier = Modifier,
            windowSizeClass = mediumTabletLandscape,
            registrationUiModel = { RegistrationUiModel.Empty }
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedPortraitPreview
@Composable
private fun TabletExpandedPortraitPreview() {
    NoteMarkTheme {
        RegistrationPane(
            modifier = Modifier,
            windowSizeClass = extendedTabletPortrait,
            registrationUiModel = { RegistrationUiModel.Empty }
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedLandscapePreview
@Composable
private fun TabletExpandedLandscapePreview() {
    NoteMarkTheme {
        RegistrationPane(
            modifier = Modifier,
            windowSizeClass = extendedTabletLandscape,
            registrationUiModel = { RegistrationUiModel.Empty }
        )
    }
}