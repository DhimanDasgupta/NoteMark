package com.dhimandasgupta.notemark.features.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.style.then
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.common.extensions.compose.DeviceLayoutType
import com.dhimandasgupta.notemark.common.extensions.compose.alignToSafeDrawing
import com.dhimandasgupta.notemark.common.extensions.compose.getDeviceLayoutType
import com.dhimandasgupta.notemark.common.extensions.compose.lifecycleAwareDebouncedClickable
import com.dhimandasgupta.notemark.features.login.LoginAction.EmailEntered
import com.dhimandasgupta.notemark.features.login.LoginAction.HideLoginButton
import com.dhimandasgupta.notemark.features.login.LoginAction.LoginClicked
import com.dhimandasgupta.notemark.features.login.LoginAction.PasswordEntered
import com.dhimandasgupta.notemark.ui.WindowSizePreviews
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkButton
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkPasswordTextField
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkStyles
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTextField
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull

@Composable
internal fun LoginPane(
  modifier: Modifier = Modifier,
  style: Style = Style,
  loginUiModel: () -> LoginUiModel,
  loginAction: (LoginAction) -> Unit = {},
  navigateToAfterLogin: () -> Unit = {},
  navigateToRegistration: () -> Unit = {},
) {
  val context = LocalContext.current
  val updatedLoginUiModel by rememberUpdatedState(newValue = loginUiModel)

  LaunchedEffect(key1 = Unit) {
    snapshotFlow { loginUiModel().loginSuccess }
      .filterNotNull()
      .collect { isSuccess ->
        val message = if (isSuccess) "Login successful" else "Login failed"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        // Always call the latest lambdas
        loginAction(LoginAction.LoginChangeConsumed)
        if (isSuccess) {
          navigateToAfterLogin()
        }
      }
  }

  Box(
    modifier =
      modifier
        .styleable(null, style)
        .background(color = colorResource(id = R.color.splash_blue))
        .fillMaxSize()
  ) {
    val layoutType = getDeviceLayoutType()

    when (layoutType) {
      DeviceLayoutType.PHONE_PORTRAIT ->
        PhonePortraitLayout(
          modifier = Modifier,
          loginUiModel = updatedLoginUiModel,
          loginAction = loginAction,
          navigateToRegistration = navigateToRegistration,
        )

      DeviceLayoutType.PHONE_LANDSCAPE ->
        PhoneLandscapeLayout(
          modifier = Modifier,
          loginUiModel = updatedLoginUiModel,
          loginAction = loginAction,
          navigateToRegistration = navigateToRegistration,
        )

      else ->
        TabletLayout(
          modifier = Modifier,
          loginUiModel = updatedLoginUiModel,
          loginAction = loginAction,
          navigateToRegistration = navigateToRegistration,
        )
    }
  }
}

@Composable
private fun PhoneLandscapeLayout(
  modifier: Modifier = Modifier,
  style: Style = Style,
  loginUiModel: () -> LoginUiModel,
  loginAction: (LoginAction) -> Unit = {},
  navigateToRegistration: () -> Unit = {},
) {
  Row(
    modifier =
      modifier
        .padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 8.dp)
        .fillMaxSize()
        .styleable(null, NoteMarkTheme.styles.authSheetStyle, style),
    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
    verticalAlignment = Alignment.Top,
  ) {
    LeftPane(
      modifier =
        Modifier.safeContentPadding()
          .fillMaxWidth(fraction = 0.4f)
          .align(Alignment.CenterVertically)
    )
    RightPane(
      modifier =
        Modifier.fillMaxHeight()
          .padding(
            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
            start =
              WindowInsets.systemBars
                .union(insets = WindowInsets.displayCutout)
                .asPaddingValues()
                .calculateLeftPadding(LayoutDirection.Ltr),
            end =
              WindowInsets.systemBars
                .union(insets = WindowInsets.displayCutout)
                .asPaddingValues()
                .calculateRightPadding(LayoutDirection.Ltr),
          )
          .verticalScroll(state = rememberScrollState()),
      navigateToRegistration = navigateToRegistration,
      loginUiModel = loginUiModel,
      loginAction = loginAction,
    )
  }
}

/** Auth sheet with the wider tablet gutters and no bottom padding. */
private val TabletAuthSheetStyle =
  NoteMarkStyles.authSheetStyle then
    Style { contentPadding(start = 64.dp, top = 64.dp, end = 64.dp, bottom = 0.dp) }

@Composable
private fun TabletLayout(
  modifier: Modifier = Modifier,
  style: Style = Style,
  loginUiModel: () -> LoginUiModel,
  loginAction: (LoginAction) -> Unit = {},
  navigateToRegistration: () -> Unit = {},
) {
  Column(
    modifier =
      modifier
        .padding(
          top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 8.dp,
          start =
            WindowInsets.systemBars.asPaddingValues().calculateLeftPadding(LayoutDirection.Ltr),
          end =
            WindowInsets.systemBars.asPaddingValues().calculateRightPadding(LayoutDirection.Ltr),
        )
        .fillMaxSize()
        .styleable(null, TabletAuthSheetStyle, style)
        .verticalScroll(state = rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(space = 8.dp),
  ) {
    LeftPane(
      modifier = Modifier.align(Alignment.CenterHorizontally),
      horizontalAlignment = Alignment.CenterHorizontally,
    )
    Spacer(modifier = Modifier.height(height = 16.dp))
    RightPane(
      navigateToRegistration = navigateToRegistration,
      loginUiModel = loginUiModel,
      loginAction = loginAction,
    )
  }
}

@Composable
private fun PhonePortraitLayout(
  modifier: Modifier = Modifier,
  style: Style = Style,
  loginUiModel: () -> LoginUiModel,
  loginAction: (LoginAction) -> Unit = {},
  navigateToRegistration: () -> Unit = {},
) {
  Column(
    modifier =
      modifier
        .padding(
          top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 8.dp,
          start =
            WindowInsets.systemBars.asPaddingValues().calculateLeftPadding(LayoutDirection.Ltr),
          end =
            WindowInsets.systemBars.asPaddingValues().calculateRightPadding(LayoutDirection.Ltr),
        )
        .fillMaxSize()
        .styleable(null, NoteMarkTheme.styles.authSheetStyle, style)
        .verticalScroll(state = rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(space = 8.dp),
  ) {
    LeftPane()
    Spacer(modifier = Modifier.height(height = 16.dp))
    RightPane(
      navigateToRegistration = navigateToRegistration,
      loginUiModel = loginUiModel,
      loginAction = loginAction,
    )
  }
}

@Composable
private fun LeftPane(
  modifier: Modifier = Modifier,
  style: Style = Style,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
  Column(
    modifier = modifier.styleable(null, style),
    verticalArrangement = Arrangement.spacedBy(space = 8.dp),
    horizontalAlignment = horizontalAlignment,
  ) {
    Text(
      text = "Log In",
      style = typography.titleLarge,
    )

    Text(
      text = "Capture your thoughts and ideas",
      style = typography.bodyLarge,
      color = colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun RightPane(
  modifier: Modifier = Modifier,
  style: Style = Style,
  loginUiModel: () -> LoginUiModel,
  navigateToRegistration: () -> Unit = {},
  loginAction: (LoginAction) -> Unit = {},
) {
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current

  LaunchedEffect(key1 = Unit) { focusManager.clearFocus() }

  Column(
    modifier = modifier.styleable(null, style),
    verticalArrangement = Arrangement.Center,
  ) {
    LoginEmailField(
      modifier = Modifier,
      loginUiModel = loginUiModel,
      loginAction = loginAction,
    )

    LoginPasswordField(
      modifier = Modifier,
      loginUiModel = loginUiModel,
      loginAction = loginAction,
      keyboardController = keyboardController,
    )

    LoginButton(
      modifier = Modifier,
      loginUiModel = loginUiModel,
      loginAction = loginAction,
      keyboardController = keyboardController,
    )

    Spacer(modifier = Modifier.height(height = 16.dp))

    LoginFooterField(
      modifier = Modifier,
      navigateToRegistration = navigateToRegistration,
    )

    Spacer(modifier = Modifier.imePadding())
  }
}

@OptIn(FlowPreview::class)
@Composable
private fun LoginEmailField(
  modifier: Modifier = Modifier,
  style: Style = Style,
  loginUiModel: () -> LoginUiModel,
  loginAction: (LoginAction) -> Unit = {},
) {
  val focusManager = LocalFocusManager.current

  var email by remember { mutableStateOf(value = loginUiModel().email) }
  LaunchedEffect(key1 = Unit) {
    snapshotFlow { email }
      .debounce(timeoutMillis = 100)
      .collectLatest { loginAction(EmailEntered(email = email)) }
  }

  NoteMarkTextField(
    modifier = modifier.fillMaxWidth().alignToSafeDrawing(),
    style = style,
    label = "Email",
    enteredText = email,
    hintText = "john.doe@gmail.com",
    onTextChanged = { value -> email = value },
    onNextClicked = { focusManager.moveFocus(FocusDirection.Next) },
  )
}

@OptIn(FlowPreview::class)
@Composable
private fun LoginPasswordField(
  modifier: Modifier = Modifier,
  style: Style = Style,
  loginUiModel: () -> LoginUiModel,
  loginAction: (LoginAction) -> Unit = {},
  keyboardController: SoftwareKeyboardController?,
) {
  val focusManager = LocalFocusManager.current

  var password by remember { mutableStateOf(value = loginUiModel().password) }
  LaunchedEffect(key1 = Unit) {
    snapshotFlow { password }
      .debounce(timeoutMillis = 100)
      .collectLatest { loginAction(PasswordEntered(password = password)) }
  }

  NoteMarkPasswordTextField(
    modifier = modifier.fillMaxWidth().alignToSafeDrawing(),
    style = style,
    label = "Password",
    enteredText = password,
    hintText = "Password",
    onTextChanged = { value -> password = value },
    onDoneClicked = {
      focusManager.moveFocus(FocusDirection.Enter)
      keyboardController?.hide()
      focusManager.clearFocus(force = true)

      if (loginUiModel().loginEnabled) {
        loginAction(HideLoginButton)
        loginAction(LoginClicked)
      }
    },
  )
}

@Composable
private fun LoginButton(
  modifier: Modifier = Modifier,
  style: Style = Style,
  loginUiModel: () -> LoginUiModel,
  loginAction: (LoginAction) -> Unit = {},
  keyboardController: SoftwareKeyboardController?,
) {
  val focusManager = LocalFocusManager.current

  // Derived so the email/password echo from the presenter only recomposes the button when the
  // enabled flag actually flips.
  val loginEnabled by remember(loginUiModel) { derivedStateOf { loginUiModel().loginEnabled } }

  NoteMarkButton(
    onClick = {
      keyboardController?.hide()
      focusManager.clearFocus(force = true)
      loginAction(HideLoginButton)
      loginAction(LoginClicked)
    },
    modifier = modifier.fillMaxWidth(),
    style = style,
    enabled = loginEnabled,
  ) {
    Text(
      text = "Log in",
      style = typography.titleSmall,
    )
  }
}

@Composable
private fun LoginFooterField(
  modifier: Modifier = Modifier,
  style: Style = Style,
  navigateToRegistration: () -> Unit = {},
) {
  Text(
    text = "Don't have an account?",
    style = typography.titleSmall,
    fontWeight = FontWeight.Normal,
    modifier =
      modifier.styleable(null, style).fillMaxSize().lifecycleAwareDebouncedClickable {
        navigateToRegistration()
      },
    textAlign = TextAlign.Center,
    color = colorScheme.primary,
  )
}

@WindowSizePreviews
@Composable
private fun LoginPanePreview(
  @PreviewParameter(provider = LoginUiModelPreviewProvider::class) defaultLoginUiModel: LoginUiModel
) {
  NoteMarkTheme {
    LoginPane(
      modifier = Modifier,
      loginUiModel = { defaultLoginUiModel },
    )
  }
}

private class LoginUiModelPreviewProvider : PreviewParameterProvider<LoginUiModel> {
  override val values: Sequence<LoginUiModel>
    get() = sequenceOf(element = LoginUiModel.defaultOrEmpty)
}
