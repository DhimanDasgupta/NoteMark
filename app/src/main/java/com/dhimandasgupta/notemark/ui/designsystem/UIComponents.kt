package com.dhimandasgupta.notemark.ui.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.common.extensions.compose.lifecycleAwareDebouncedClickable
import com.dhimandasgupta.notemark.common.extensions.compose.trackRecompositions

@Composable
fun NoteMarkButton(
  modifier: Modifier = Modifier,
  enabled: Boolean = false,
  onClick: () -> Unit = {},
  content: @Composable RowScope.() -> Unit,
) {
  Button(
    modifier = modifier,
    onClick = onClick,
    shape = RoundedCornerShape(size = 8.dp),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = colorScheme.primary,
        disabledContainerColor = colorScheme.onSurface.copy(alpha = 0.12f),
      ),
    enabled = enabled,
  ) {
    content()
  }
}

@Composable
fun NoteMarkOutlinedButton(
  modifier: Modifier = Modifier,
  enabled: Boolean = false,
  onClick: () -> Unit = {},
  content: @Composable RowScope.() -> Unit,
) {
  OutlinedButton(
    modifier = modifier,
    onClick = onClick,
    shape = RoundedCornerShape(size = 8.dp),
    border = BorderStroke(width = 1.dp, color = colorScheme.primary),
    enabled = enabled,
  ) {
    content()
  }
}

@Composable
fun NoteMarkTextField(
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  label: String? = "",
  enteredText: String = "",
  hintText: String = "",
  explanationText: String = "",
  errorText: String = "",
  onTextChanged: (String) -> Unit = {},
  onFocusGained: () -> Unit = {},
  onFocusLost: () -> Unit = {},
  onNextClicked: (() -> Unit)? = null,
  onDoneClicked: (() -> Unit)? = null,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    horizontalAlignment = Alignment.Start,
  ) {
    AnimatedVisibility(visible = label?.isNotEmpty() == true) {
      label?.let {
        Text(
          text = label,
          style = typography.bodyMedium,
        )
      }
    }

    var hasFocus by retain { mutableStateOf(value = false) }

    TextField(
      enabled = enabled,
      value = enteredText,
      onValueChange = onTextChanged,
      modifier =
        Modifier.fillMaxWidth()
          .clip(Shapes.medium)
          .border(
            width = if (hasFocus) 1.dp else 0.dp,
            color = if (hasFocus) colorScheme.primary else colorScheme.surface,
            shape = Shapes.medium,
          )
          .onFocusChanged { focusState ->
            hasFocus = focusState.hasFocus
            if (focusState.hasFocus) onFocusGained() else onFocusLost()
          },
      visualTransformation = VisualTransformation.None,
      placeholder = { Text(hintText) },
      maxLines = 1,
      colors =
        OutlinedTextFieldDefaults.colors()
          .copy(
            focusedContainerColor = colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
          ),
      keyboardOptions =
        KeyboardOptions(
          keyboardType = KeyboardType.Unspecified,
          imeAction = onDoneClicked?.let { ImeAction.Done } ?: ImeAction.Next,
        ),
      keyboardActions =
        KeyboardActions(
          onNext = { onNextClicked?.invoke() },
          onDone = { onDoneClicked?.invoke() },
        ),
    )

    AnimatedVisibility(visible = explanationText.isNotEmpty()) {
      Text(
        text = explanationText,
        style = typography.bodySmall,
        color = colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 4.dp),
      )
    }

    AnimatedVisibility(visible = errorText.isNotEmpty()) {
      Text(
        text = errorText,
        style = typography.bodySmall,
        color = colorScheme.error,
      )
    }

    Spacer(modifier = Modifier.height(height = 8.dp))
  }
}

@Composable
fun NoteMarkPasswordTextField(
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  label: String? = "",
  enteredText: String = "",
  hintText: String = "",
  explanationText: String = "",
  errorText: String = "",
  onTextChanged: (String) -> Unit = {},
  onFocusGained: () -> Unit = {},
  onFocusLost: () -> Unit = {},
  onNextClicked: (() -> Unit)? = null,
  onDoneClicked: (() -> Unit)? = null,
) {
  var hasFocus by retain { mutableStateOf(value = false) }
  var showPassword by retain { mutableStateOf(value = false) }

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    horizontalAlignment = Alignment.Start,
  ) {
    AnimatedVisibility(visible = label?.isNotEmpty() == true) {
      label?.let {
        Text(
          text = label,
          style = typography.bodyMedium,
        )
      }
    }

    TextField(
      enabled = enabled,
      value = enteredText,
      onValueChange = onTextChanged,
      trailingIcon = {
        if (showPassword) {
          Icon(
            painter = painterResource(id = R.drawable.ic_eye_open),
            modifier =
              Modifier.size(size = 32.dp).padding(all = 4.dp).lifecycleAwareDebouncedClickable {
                showPassword = !showPassword
              },
            contentDescription = "Hide Password",
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
          )
        } else {
          Icon(
            painter = painterResource(R.drawable.ic_eye_off),
            modifier =
              Modifier.size(size = 32.dp).padding(all = 4.dp).lifecycleAwareDebouncedClickable {
                showPassword = !showPassword
              },
            contentDescription = "Show Password",
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
          )
        }
      },
      modifier =
        Modifier.fillMaxWidth()
          .clip(Shapes.medium)
          .border(
            width = if (hasFocus) 1.dp else 0.dp,
            color = if (hasFocus) colorScheme.primary else colorScheme.surface,
            shape = Shapes.medium,
          )
          .onFocusChanged { focusState ->
            hasFocus = focusState.hasFocus
            if (focusState.hasFocus) onFocusGained() else onFocusLost()
          },
      visualTransformation =
        if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
      placeholder = { Text(hintText) },
      maxLines = 1,
      colors =
        OutlinedTextFieldDefaults.colors()
          .copy(
            focusedContainerColor = colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            unfocusedPlaceholderColor = colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = colorScheme.onSurface,
          ),
      keyboardOptions =
        KeyboardOptions(
          keyboardType = KeyboardType.Password,
          imeAction = onDoneClicked?.let { ImeAction.Done } ?: ImeAction.Next,
        ),
      keyboardActions =
        KeyboardActions(
          onNext = { onNextClicked?.invoke() },
          onDone = { onDoneClicked?.invoke() },
        ),
    )

    AnimatedVisibility(visible = explanationText.isNotEmpty()) {
      Text(
        text = explanationText,
        style = typography.bodySmall,
        color = colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 4.dp),
      )
    }

    AnimatedVisibility(visible = errorText.isNotEmpty()) {
      Text(
        text = errorText,
        style = typography.bodySmall,
        color = colorScheme.error,
      )
    }

    Spacer(modifier = Modifier.height(height = 8.dp))
  }
}

@Composable
fun NoteMarkToolbarButton(
  modifier: Modifier = Modifier,
  title: String,
  isConnected: Boolean,
  onClick: () -> Unit = {},
) {
  Box(
    modifier =
      modifier
        .clip(shape = shapes.extraSmall)
        .background(
          color = if (isConnected) colorScheme.primary else colorScheme.primary.copy(alpha = 0.5f)
        )
        .lifecycleAwareDebouncedClickable {
          onClick()
        }
  ) {
    Text(
      text = title.uppercase(),
      style = typography.titleMedium,
      color = if (isConnected) colorScheme.onPrimary else colorScheme.error,
      modifier = modifier.padding(all = 4.dp),
    )
  }
}

@Composable
fun NoteMarkFAB(
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  FloatingActionButton(
    onClick = onClick,
    shape = shapes.medium,
    modifier =
      modifier
        .padding(
          end =
            WindowInsets.navigationBars
              .union(insets = WindowInsets.displayCutout)
              .asPaddingValues()
              .calculateEndPadding(LayoutDirection.Ltr),
          bottom =
            WindowInsets.navigationBars
              .union(insets = WindowInsets.displayCutout)
              .asPaddingValues()
              .calculateBottomPadding(),
        )
        .shadow(
          elevation = 8.dp,
          shape = shapes.medium,
        )
        .background(
          brush =
            Brush.verticalGradient(
              colors =
                listOf(
                  Color(color = 0XFF58A1F8),
                  Color(color = 0xFF5A4CF7),
                )
            ),
          shape = shapes.medium,
        )
        .innerShadow(
          shape = shapes.medium,
          shadow =
            Shadow(
              radius = 2.dp,
              color = colorScheme.onPrimary,
              spread = 2.dp,
              alpha = 0.5f,
            ),
        ),
    elevation =
      FloatingActionButtonDefaults.elevation(
        defaultElevation = 0.dp,
        pressedElevation = 0.dp,
        hoveredElevation = 0.dp,
        focusedElevation = 0.dp,
      ),
    contentColor = Color.Transparent,
    containerColor = Color.Transparent,
  ) {
    Icon(
      painter = painterResource(id = R.drawable.ic_plus_icon),
      contentDescription = "Add Note",
      tint = colorScheme.onPrimary,
      modifier = Modifier.padding(all = 8.dp),
    )
  }
}

@Composable
fun LimitedText(
  fullText: String,
  style: TextStyle,
  color: Color,
  targetCharacterCount: Int = 100,
) {
  // Truncation is a pure function of the inputs, so it is resolved during composition. Deriving it
  // from onTextLayout instead would write layout results back into composition, forcing a second
  // composition and layout pass for every item.
  val textToDisplay =
    remember(key1 = fullText, key2 = targetCharacterCount) {
      if (fullText.length > targetCharacterCount) fullText.take(targetCharacterCount) else fullText
    }

  Text(
    text = textToDisplay,
    style = style,
    color = color,
    maxLines = 5,
    overflow = TextOverflow.Ellipsis,
  )
}

@Composable
private fun BouncingDot(
  modifier: Modifier = Modifier,
  color: Color = Color.Blue,
  size: Dp = 10.dp,
  bounceHeight: Dp = 2.dp,
  animationDurationMillis: Int = 500,
  delayMillis: Int = 0, // Delay before this specific dot starts its animation
) {
  // Dp.value is a raw dp number, so converting to px here keeps the bounce the same physical
  // height on every density instead of shrinking as density rises.
  val bounceHeightPx = with(receiver = LocalDensity.current) { bounceHeight.toPx() }

  val transition = rememberInfiniteTransition(label = "BouncingDot")
  val offsetY by
    transition.animateFloat(
      initialValue = 0f,
      targetValue = -bounceHeightPx / 2,
      animationSpec =
        infiniteRepeatable(
          animation =
            tween(
              durationMillis = animationDurationMillis / 2,
              easing = FastOutSlowInEasing,
            ),
          repeatMode = RepeatMode.Reverse,
          initialStartOffset = StartOffset(offsetMillis = delayMillis),
        ),
      label = "offsetY",
    )

  Box(
    modifier =
      modifier
        .offset { IntOffset(x = 0, y = offsetY.toInt()) }
        .size(size)
        .clip(CircleShape)
        .background(color)
  )
}

@Composable
fun ThreeBouncingDots(
  modifier: Modifier = Modifier,
  dotColor1: Color = Color.Red,
  dotColor2: Color = Color.Green,
  dotColor3: Color = Color.Blue,
  dotSize: Dp = 12.dp,
  bounceHeight: Dp = 40.dp,
  animationDurationMillis: Int = 600,
  spaceBetweenDots: Dp = 8.dp,
  dotStartDelayMillis: Int = 150, // Staggered delay for each dot
) {
  Row(
    modifier =
      modifier.testTag(tag = ThreeBouncingDotsTag.THREE_BOUNCING_DOTS).trackRecompositions(),
    verticalAlignment = Alignment.Bottom, // Align to bottom so they bounce from the same baseline
    horizontalArrangement = Arrangement.spacedBy(space = spaceBetweenDots),
  ) {
    BouncingDot(
      color = dotColor1,
      size = dotSize,
      bounceHeight = bounceHeight,
      animationDurationMillis = animationDurationMillis,
      delayMillis = 0, // First dot starts immediately
    )
    BouncingDot(
      color = dotColor2,
      size = dotSize,
      bounceHeight = bounceHeight,
      animationDurationMillis = animationDurationMillis,
      delayMillis = dotStartDelayMillis, // Second dot is delayed
    )
    BouncingDot(
      color = dotColor3,
      size = dotSize,
      bounceHeight = bounceHeight,
      animationDurationMillis = animationDurationMillis,
      delayMillis = dotStartDelayMillis * 2, // Third dot is further delayed
    )
  }
}

object ThreeBouncingDotsTag {
  const val THREE_BOUNCING_DOTS = "ThreeBouncingDots"
}

@Composable
fun SafeIconButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
  activeState: Lifecycle.State = Lifecycle.State.RESUMED,
  debounceIntervalMs: Long = 1000L,
  content: @Composable () -> Unit,
) {
  val owner = LocalLifecycleOwner.current

  val currentOnClick by rememberUpdatedState(newValue = onClick)
  var lastClickTime by remember { mutableLongStateOf(value = 0L) }
  var lifecycleAllowsClick by remember {
    mutableStateOf(value = owner.lifecycle.currentState.isAtLeast(activeState))
  }

  DisposableEffect(key1 = owner, key2 = activeState) {
    val observer = LifecycleEventObserver { _, _ ->
      lifecycleAllowsClick = owner.lifecycle.currentState.isAtLeast(activeState)
    }
    owner.lifecycle.addObserver(observer)
    onDispose {
      owner.lifecycle.removeObserver(observer)
    }
  }

  val isButtonEnabled = enabled && lifecycleAllowsClick

  IconButton(
    onClick = {
      // Check enabled state again here, though IconButton's internal state should also prevent it
      if (isButtonEnabled) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceIntervalMs) {
          currentOnClick()
        }
      }
    },
    modifier = modifier,
    enabled = isButtonEnabled, // Pass the combined enabled state to the actual IconButton
    colors = colors,
  ) {
    content()
  }
}
