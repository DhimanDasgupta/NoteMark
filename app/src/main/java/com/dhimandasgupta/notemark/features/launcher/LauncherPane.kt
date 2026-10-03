package com.dhimandasgupta.notemark.features.launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.style.then
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkStyles
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import com.dhimandasgupta.notemark.ui.designsystem.compose.DeviceLayoutType
import com.dhimandasgupta.notemark.ui.designsystem.compose.NoteMarkButton
import com.dhimandasgupta.notemark.ui.designsystem.compose.NoteMarkOutlinedButton
import com.dhimandasgupta.notemark.ui.designsystem.compose.WindowSizePreviews
import com.dhimandasgupta.notemark.ui.designsystem.compose.getDeviceLayoutType
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
internal fun LauncherPane(
  modifier: Modifier = Modifier,
  style: Style = Style,
  launcherUiModel: () -> LauncherUiModel,
  navigateToAfterLogin: () -> Unit = {},
  navigateToLogin: () -> Unit = {},
  navigateToList: () -> Unit = {},
) {
  val updatedLauncherUiModel by rememberUpdatedState(newValue = launcherUiModel)
  val updatedNavigateToList by rememberUpdatedState(newValue = navigateToList)
  val updatedNavigateToLogin by rememberUpdatedState(newValue = navigateToLogin)

  LaunchedEffect(key1 = Unit) {
    snapshotFlow { updatedLauncherUiModel().loggedInUser }
      .collect { loggedInUser ->
        if (loggedInUser != null) {
          updatedNavigateToList()
        }
      }
  }

  // The radius is only ever read inside `graphicsLayer`, so each tick invalidates the draw phase
  // alone. The ticker is tied to STARTED so it stops writing snapshot state (and re-rendering the
  // blur layer) while the activity is in the background, where nothing can be drawn.
  var blurRadius by remember { mutableFloatStateOf(0f) }
  val lifecycleOwner = LocalLifecycleOwner.current
  LaunchedEffect(key1 = lifecycleOwner) {
    lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
      var step = 1f
      while (isActive) {
        if (blurRadius == 10f) {
          step = -1f
        } else if (blurRadius == 0f) {
          step = 1f
        }

        delay(200.milliseconds)
        blurRadius += step
      }
    }
  }

  Box(
    modifier =
      modifier.styleable(null, NoteMarkTheme.styles.landingBackgroundStyle, style).fillMaxSize()
  ) {
    when (val layoutType = getDeviceLayoutType()) {
      DeviceLayoutType.PHONE_PORTRAIT -> {
        LandingPanePortrait(
          navigateToLogin = updatedNavigateToLogin,
          deviceLayoutType = layoutType,
          navigateToAfterLogin = navigateToAfterLogin,
          radius = { blurRadius },
        )
      }

      DeviceLayoutType.PHONE_LANDSCAPE -> {
        LandingPaneLandscape(
          navigateToLogin = updatedNavigateToLogin,
          deviceLayoutType = layoutType,
          navigateToAfterLogin = navigateToAfterLogin,
          radius = { blurRadius },
        )
      }

      DeviceLayoutType.TABLET_LAYOUT -> {
        LandingPaneTablet(
          navigateToLogin = updatedNavigateToLogin,
          deviceLayoutType = layoutType,
          navigateToAfterLogin = navigateToAfterLogin,
          radius = { blurRadius },
        )
      }
    }
  }
}

@Composable
private fun LandingPanePortrait(
  modifier: Modifier = Modifier,
  style: Style = Style,
  deviceLayoutType: DeviceLayoutType,
  navigateToAfterLogin: () -> Unit = {},
  navigateToLogin: () -> Unit = {},
  radius: () -> Float,
) {
  Box(
    modifier = modifier.styleable(null, style).fillMaxSize(),
    contentAlignment = Alignment.BottomCenter,
  ) {
    Image(
      painter = painterResource(id = R.drawable.bg_phone_portrait),
      contentDescription = null,
      contentScale = ContentScale.FillHeight,
      modifier =
        Modifier.aspectRatio(ratio = 0.8f).align(Alignment.TopCenter).graphicsLayer {
          applyBlurEffect(radius)
        },
    )

    ForegroundPane(
      modifier =
        Modifier.styleable(null, NoteMarkTheme.styles.landingSheetStyle)
          .padding(
            bottom =
              WindowInsets.displayCutout
                .union(insets = WindowInsets.navigationBars)
                .asPaddingValues()
                .calculateBottomPadding()
          ),
      navigateToLogin = navigateToLogin,
      deviceLayoutType = deviceLayoutType,
      navigateToAfterLogin = navigateToAfterLogin,
    )
  }
}

/** Landing sheet docked to the end edge, with the end gutter left to window insets. */
private val LandscapeLandingSheetStyle =
  NoteMarkStyles.landingSheetStyle then
    Style {
      shape(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
      contentPadding(start = 32.dp, top = 32.dp, end = 0.dp, bottom = 32.dp)
    }

@Composable
private fun LandingPaneLandscape(
  modifier: Modifier = Modifier,
  style: Style = Style,
  deviceLayoutType: DeviceLayoutType,
  navigateToAfterLogin: () -> Unit = {},
  navigateToLogin: () -> Unit = {},
  radius: () -> Float,
) {
  Row(
    modifier = modifier.styleable(null, style).fillMaxSize(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      painter = painterResource(id = R.drawable.bg_phone_landscape),
      contentDescription = null,
      contentScale = ContentScale.FillHeight,
      modifier =
        Modifier.fillMaxHeight(0.65f).graphicsLayer {
          applyBlurEffect(radius)
        },
    )

    ForegroundPane(
      modifier =
        Modifier.wrapContentSize(align = Alignment.Center)
          .styleable(null, LandscapeLandingSheetStyle)
          .padding(
            end =
              WindowInsets.displayCutout
                .union(insets = WindowInsets.navigationBars)
                .asPaddingValues()
                .calculateRightPadding(LayoutDirection.Ltr)
          )
          .fillMaxHeight(fraction = 0.85f),
      navigateToLogin = navigateToLogin,
      deviceLayoutType = deviceLayoutType,
      navigateToAfterLogin = navigateToAfterLogin,
    )
  }
}

@Composable
private fun LandingPaneTablet(
  modifier: Modifier = Modifier,
  style: Style = Style,
  deviceLayoutType: DeviceLayoutType,
  navigateToAfterLogin: () -> Unit = {},
  navigateToLogin: () -> Unit = {},
  radius: () -> Float,
) {
  Column(
    modifier = modifier.styleable(null, style).fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Image(
      painter = painterResource(id = R.drawable.bg_phone_landscape),
      contentDescription = null,
      contentScale = ContentScale.Fit,
      modifier =
        Modifier.weight(weight = 1f).aspectRatio(ratio = 0.5f).graphicsLayer {
          applyBlurEffect(radius)
        },
    )

    ForegroundPane(
      modifier =
        Modifier.fillMaxWidth(fraction = 0.85f)
          .styleable(null, NoteMarkTheme.styles.landingSheetStyle)
          .padding(
            bottom =
              WindowInsets.navigationBars
                .union(insets = WindowInsets.displayCutout)
                .asPaddingValues()
                .calculateBottomPadding()
          ),
      navigateToLogin = navigateToLogin,
      deviceLayoutType = deviceLayoutType,
      navigateToAfterLogin = navigateToAfterLogin,
    )
  }
}

@Composable
private fun ForegroundPane(
  modifier: Modifier = Modifier,
  style: Style = Style,
  navigateToAfterLogin: () -> Unit = {},
  navigateToLogin: () -> Unit = {},
  deviceLayoutType: DeviceLayoutType,
) {
  Column(
    modifier = modifier.styleable(null, style).padding(top = 8.dp, bottom = 16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment =
      when (deviceLayoutType) {
        DeviceLayoutType.TABLET_LAYOUT -> Alignment.CenterHorizontally
        else -> Alignment.Start
      },
  ) {
    Text(
      text = stringResource(R.string.landing_info_one),
      style = typography.titleLarge,
      color = colorScheme.onSurface,
      modifier =
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).wrapContentSize(Alignment.Center),
    )

    Spacer(Modifier.height(height = 8.dp))

    Text(
      text = stringResource(R.string.landing_info_two),
      style = typography.bodyLarge,
      color = colorScheme.onSurfaceVariant,
      modifier =
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).wrapContentSize(Alignment.Center),
    )

    Spacer(Modifier.height(height = 24.dp))

    NoteMarkButton(
      onClick = navigateToAfterLogin,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      enabled = true,
    ) {
      Text(
        text = "Get Started",
        style = typography.titleSmall,
        modifier = Modifier.requiredHeight(IntrinsicSize.Min),
      )
    }

    Spacer(Modifier.height(height = 8.dp))

    NoteMarkOutlinedButton(
      onClick = { navigateToLogin() },
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      enabled = true,
    ) {
      Text(
        text = "Log in",
        style = typography.titleSmall,
        modifier = Modifier.requiredHeight(IntrinsicSize.Min),
      )
    }
  }
}

private fun GraphicsLayerScope.applyBlurEffect(radius: () -> Float) {
  val radius = radius()
  if (radius > 0) {
    renderEffect =
      BlurEffect(
        radiusX = radius,
        radiusY = radius,
        edgeTreatment = TileMode.Clamp,
      )
  }
}

@WindowSizePreviews
@Composable
private fun LauncherPanePreview(
  @PreviewParameter(provider = LauncherUiModelPreviewProvider::class)
  defaultLauncherUiModel: LauncherUiModel
) {
  NoteMarkTheme {
    LauncherPane(
      modifier = Modifier,
      launcherUiModel = { defaultLauncherUiModel },
    )
  }
}

private class LauncherUiModelPreviewProvider : PreviewParameterProvider<LauncherUiModel> {
  override val values: Sequence<LauncherUiModel>
    get() = sequenceOf(element = LauncherUiModel.defaultOrEmpty)
}
