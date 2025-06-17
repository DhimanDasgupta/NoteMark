package com.dhimandasgupta.notemark.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
                    if (focusState.hasFocus) onFocusGained
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

        if (explanationText.isNotBlank()) {
            Text(text = explanationText, style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
        }

        if (errorText.isNotBlank()) {
            Text(text = errorText, style = typography.bodySmall, color = colorScheme.error)
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
                        painter = painterResource(R.drawable.ic_eye_off),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                showPassword = !showPassword
                            }
                        ,
                        contentDescription = "Hide Password",
                        tint = colorScheme.onSurfaceVariant
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_eye_open),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                showPassword = !showPassword
                            }
                        ,
                        contentDescription = "Show Password",
                        tint = colorScheme.onSurfaceVariant
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
                    if (focusState.hasFocus) onFocusGained
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
            Text(text = explanationText, style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
        }

        if (errorText.isNotBlank()) {
            Text(text = errorText, style = typography.bodySmall, color = colorScheme.error)
        }
    }
}