package com.dhimandasgupta.notemark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.statemachine.AppState
import com.dhimandasgupta.notemark.statemachine.LoggedInState
import com.dhimandasgupta.notemark.statemachine.NonLoggedInState
import com.dhimandasgupta.notemark.ui.PhoneLandscapePreview
import com.dhimandasgupta.notemark.ui.PhonePortraitPreview
import com.dhimandasgupta.notemark.ui.TabletExpandedLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletExpandedPortraitPreview
import com.dhimandasgupta.notemark.ui.TabletMediumLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletMediumPortraitPreview
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkOutlinedButton
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import com.dhimandasgupta.notemark.ui.extendedTabletLandscape
import com.dhimandasgupta.notemark.ui.extendedTabletPortrait
import com.dhimandasgupta.notemark.ui.mediumTabletLandscape
import com.dhimandasgupta.notemark.ui.mediumTabletPortrait
import com.dhimandasgupta.notemark.ui.phoneLandscape
import com.dhimandasgupta.notemark.ui.phonePortrait

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun LoggedInPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    appState: AppState,
    logoutClicked: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .background(color = colorResource(R.color.splash_blue_background))
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (appState) {
            is NonLoggedInState -> {
                Text("Upcoming....", style = MaterialTheme.typography.displayLarge)
            }
            is LoggedInState -> {
                NoteMarkOutlinedButton(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 16.dp),
                    onClick = logoutClicked,
                    enabled = true
                ) {
                    Text("Logout ${appState.loggedInUser}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhonePortraitPreview
@Composable
private fun PhonePortraitPreview() {
    NoteMarkTheme {
        LoggedInPane(
            modifier = Modifier,
            windowSizeClass = phonePortrait,
            appState = NonLoggedInState(null)
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhoneLandscapePreview
@Composable
private fun PhoneLandscapePreview() {
    NoteMarkTheme {
        LoggedInPane(
            modifier = Modifier,
            windowSizeClass = phoneLandscape,
            appState = NonLoggedInState(null)
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumPortraitPreview
@Composable
private fun TabletMediumPortraitPreview() {
    NoteMarkTheme {
        LoggedInPane(
            modifier = Modifier,
            windowSizeClass = mediumTabletPortrait,
            appState = NonLoggedInState(null)
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumLandscapePreview
@Composable
private fun TabletMediumLandscapePreview() {
    NoteMarkTheme {
        LoggedInPane(
            modifier = Modifier,
            windowSizeClass = mediumTabletLandscape,
            appState = NonLoggedInState(null)
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedPortraitPreview
@Composable
private fun TabletExpandedPortraitPreview() {
    NoteMarkTheme {
        LoggedInPane(
            modifier = Modifier,
            windowSizeClass = extendedTabletPortrait,
            appState = NonLoggedInState(null)
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedLandscapePreview
@Composable
private fun TabletExpandedLandscapePreview() {
    NoteMarkTheme {
        LoggedInPane(
            modifier = Modifier,
            windowSizeClass = extendedTabletLandscape,
            appState = NonLoggedInState(null)
        )
    }
}