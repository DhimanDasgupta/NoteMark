package com.dhimandasgupta.notemark.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R

@Composable
fun NoteMarkButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, disabledContainerColor = colorScheme.onSurface.copy(alpha = 0.12f)),
        enabled = enabled
    ) {
        content()
    }
}

@Composable
fun NoteMarkOutlinedButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colorScheme.primary),
        enabled = enabled
    ) {
         content()
    }
}

@Composable
fun NoteMarkTextField(
    modifier: Modifier = Modifier,
    label: String? = "",
    enteredText: String = "",
    hintText: String = "",
    explanationText: String = "",
    errorText: String = "",
    onTextChanged: (String) -> Unit = {},
    onFocusGained: () -> Unit = {},
    onFocusLost: () -> Unit = {},
    onNextClicked: (() -> Unit)? = null,
    onDoneClicked: (() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        label?.let {
            Text(
                text = label,
                style = typography.bodyMedium
            )
        }

        var hasFocus by rememberSaveable { mutableStateOf(false) }

        TextField(
            value = enteredText,
            onValueChange = onTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .clip(Shapes.medium)
                .border(
                    width = if (hasFocus) 1.dp else 0.dp,
                    color = if (hasFocus) colorScheme.primary else colorScheme.surface,
                    shape = Shapes.medium
                )
                .onFocusChanged { focusState ->
                    hasFocus = focusState.hasFocus
                    if (focusState.hasFocus) onFocusGained() else onFocusLost()
                },
            visualTransformation = VisualTransformation.None,
            placeholder = { Text(hintText) },
            maxLines = 1,
            colors = OutlinedTextFieldDefaults.colors().copy(
                focusedContainerColor = colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Unspecified,
                imeAction = onDoneClicked?.let { ImeAction.Done } ?: ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNextClicked?.invoke() },
                onDone = { onDoneClicked?.invoke() }
            )
        )

        if (explanationText.isNotEmpty()) {
            Text(
                text = explanationText,
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (errorText.isNotEmpty()) {
            Text(
                text = errorText,
                style = typography.bodySmall,
                color = colorScheme.error
            )
        }
    }
}

@Composable
fun NoteMarkPasswordTextField(
    modifier: Modifier = Modifier,
    label: String? = "",
    enteredText: String = "",
    hintText: String = "",
    explanationText: String = "",
    errorText: String = "",
    onTextChanged: (String) -> Unit = {},
    onFocusGained: () -> Unit = {},
    onFocusLost: () -> Unit = {},
    onNextClicked: (() -> Unit)? = null,
    onDoneClicked: (() -> Unit)? = null
) {
    var hasFocus by rememberSaveable { mutableStateOf(false) }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        label?.let {
            Text(
                text = label,
                style = typography.bodyMedium
            )
        }

        TextField(
            value = enteredText,
            onValueChange = onTextChanged,
            trailingIcon = {
                if (showPassword) {
                    Icon(
                        painter = painterResource(R.drawable.ic_eye_open),
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                            .clickable {
                                showPassword = !showPassword
                            }
                        ,
                        contentDescription = "Hide Password",
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_eye_off),
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                            .clickable {
                                showPassword = !showPassword
                            }
                        ,
                        contentDescription = "Show Password",
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(Shapes.medium)
                .border(
                    width = if (hasFocus) 1.dp else 0.dp,
                    color = if (hasFocus) colorScheme.primary else colorScheme.surface,
                    shape = Shapes.medium
                )
                .onFocusChanged { focusState ->
                    hasFocus = focusState.hasFocus
                    if (focusState.hasFocus) onFocusGained() else onFocusLost()
                },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            placeholder = { Text(hintText) },
            maxLines = 1,
            colors = OutlinedTextFieldDefaults.colors().copy(
                focusedContainerColor = colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                unfocusedPlaceholderColor = colorScheme.onSurfaceVariant,
                focusedPlaceholderColor = colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = onDoneClicked?.let { ImeAction.Done } ?: ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNextClicked?.invoke() },
                onDone = { onDoneClicked?.invoke() }
            )
        )

        if (explanationText.isNotEmpty()) {
            Text(
                text = explanationText,
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (errorText.isNotEmpty()) {
            Text(
                text = errorText,
                style = typography.bodySmall,
                color = colorScheme.error
            )
        }
    }
}

@Composable
fun NoteMarkToolbarButton(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(shape = shapes.extraSmall)
            .background(color = colorScheme.primary)
            .clickable {
                onClick()
            }
    ) {
        Text(
            text = title.uppercase(),
            style = typography.titleMedium,
            color = colorScheme.onPrimary,
            modifier = modifier.padding(all = 4.dp)
        )
    }
}

@Composable
fun NoteMarkFAB(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    FloatingActionButton(
        onClick = onClick,
        shape = shapes.medium,
        modifier = modifier
            .padding(
                end =  WindowInsets.navigationBars.union(WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateEndPadding(LayoutDirection.Ltr),
                bottom = WindowInsets.navigationBars.union(WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0XFF58A1F8), Color(0xFF5A4CF7))
                ),
                shape = shapes.medium
            ),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = 0.dp,
            focusedElevation = 0.dp
        ),
        contentColor = Color.Transparent,
        containerColor = Color.Transparent
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus_icon),
            contentDescription = "Add Note",
            tint = colorScheme.onPrimary,
            modifier = Modifier
                .padding(all = 8.dp)
        )
    }
}

@Composable
fun LimitedText(
    fullText: String,
    style: TextStyle,
    color: Color,
    targetCharCount: Int = 100
) {
    var textToDisplay by remember(fullText) { mutableStateOf(fullText) }
    var textLayoutResultState by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = textToDisplay,
        style = style,
        color = color,
        onTextLayout = { textLayoutResult ->
            textLayoutResultState = textLayoutResult
            if (textLayoutResult.layoutInput.text.length > targetCharCount) {
                if (textLayoutResult.isLineEllipsized(textLayoutResult.lineCount - 1) ||
                    textLayoutResult.getLineEnd(
                        textLayoutResult.lineCount - 1,
                        visibleEnd = true
                    ) < targetCharCount &&
                    fullText.length > targetCharCount
                ) {
                    if (textToDisplay.length > targetCharCount) { // Ensure we only shorten once
                        textToDisplay = fullText.substring(0, targetCharCount)
                    }
                } else if (fullText.length > targetCharCount && textToDisplay.length > targetCharCount) {
                    textToDisplay = fullText.substring(0, targetCharCount)
                }
            }
        },
        maxLines = 5,
        overflow = TextOverflow.Ellipsis
    )
}