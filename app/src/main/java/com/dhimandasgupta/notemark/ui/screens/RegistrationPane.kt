package com.dhimandasgupta.notemark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R

@Preview
@Composable
fun RegistrationPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    navigateToLogin: () -> Unit = {},
    navigateToRegistration: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .background(color = colorResource(R.color.splash_blue))
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                    start = WindowInsets.systemBars.asPaddingValues().calculateLeftPadding(LayoutDirection.Ltr),
                    end = WindowInsets.systemBars.asPaddingValues().calculateRightPadding(LayoutDirection.Ltr)
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
            Text(
                text = "Create account",
                style = typography.headlineLarge
            )

            Text(
                text = "Capture your thoughts and ideas",
                style = typography.bodySmall,
                color = typography.bodySmall.color.copy(alpha = 0.5f)
            )

            Text(
                text = "Username",
                style = typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxSize(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Unspecified,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                )
            )

            Text(
                text = "Email",
                style = typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxSize(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                )
            )

            Text(
                text = "Password",
                style = typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxSize(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                )
            )

            Text(
                text = "Repeat password",
                style = typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxSize(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus(true) }
                )
            )

            Button(
                onClick = {},
                modifier = modifier
                    .fillMaxWidth(),
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
}