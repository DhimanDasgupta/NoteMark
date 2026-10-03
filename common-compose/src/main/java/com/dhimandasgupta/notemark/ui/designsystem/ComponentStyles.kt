package com.dhimandasgupta.notemark.ui.designsystem

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.StyleStateKey
import androidx.compose.foundation.style.animate
import androidx.compose.foundation.style.border
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.contentPaddingVertical
import androidx.compose.foundation.style.fillWidth
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.pressed
import androidx.compose.foundation.style.size
import androidx.compose.foundation.style.state
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.dp

val StyleScope.colorScheme: ColorScheme
  get() = LocalNoteMarkTheme.currentValue.colorScheme

val StyleScope.shapes: Shapes
  get() = LocalNoteMarkTheme.currentValue.shapes

/** Set on a component's style state while the device has no network connection. */
val DisconnectedStateKey = StyleStateKey(defaultValue = false)

fun StyleScope.disconnected(block: StyleScope.() -> Unit) = state(DisconnectedStateKey) { block() }

object NoteMarkStyles {
  /** Default surface behind a full screen. */
  val screenStyle: Style = Style {
    background(colorScheme.surfaceContainerLowest)
  }

  /** Slightly darker surface behind the note grid, so the note cards stand out. */
  val noteListScreenStyle: Style = Style {
    background(colorScheme.surfaceContainerLow)
  }

  /** Brand-blue backdrop behind the login and registration sheets. */
  val authBackgroundStyle: Style = Style {
    background(SplashBlue)
  }

  /** Pale-blue backdrop behind the landing screen artwork. */
  val landingBackgroundStyle: Style = Style {
    background(SplashBlueBackground)
  }

  /**
   * Top-rounded sheet holding the landing screen copy and buttons. The bottom gutter comes from
   * window insets, so callers add it on their modifier.
   */
  val landingSheetStyle: Style = Style {
    shape(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    clip()
    background(colorScheme.surface)
    contentPadding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp)
  }

  /** Input box of a text field; the border picks up the primary color while focused. */
  val textFieldStyle: Style = Style {
    shape(shapes.medium)
    clip()
    border(1.dp, colorScheme.surface)
    focused {
      animate {
        borderColor(colorScheme.primary)
      }
    }
  }

  /** Gradient-filled container of the add-note FAB. */
  val fabStyle: Style = Style {
    shape(shapes.medium)
    background(Brush.verticalGradient(listOf(Color(0xFF58A1F8), Color(0xFF5A4CF7))))
    innerShadow(
      Shadow(
        radius = 2.dp,
        color = colorScheme.onPrimary,
        spread = 2.dp,
        alpha = 0.5f,
      )
    )
  }

  /** One dot of the loading indicator. */
  val loadingDotStyle: Style = Style {
    size(12.dp)
    shape(CircleShape)
    clip()
    background(SplashBlue)
  }

  /** Top-rounded surface that hosts the login and registration forms. */
  val authSheetStyle: Style = Style {
    shape(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    clip()
    background(colorScheme.surfaceContainerLowest)
    contentPadding(16.dp)
  }

  /** Card for a single note in the note list grid. */
  val noteCardStyle: Style = Style {
    shape(shapes.medium)
    clip()
    background(colorScheme.surfaceContainerLowest)
    innerShadow(
      Shadow(
        radius = 12.dp,
        color = colorScheme.primary,
        spread = 4.dp,
        alpha = 0.4f,
      )
    )
    contentPadding(16.dp)
    // Pressed state layer, matching the Material 3 pressed overlay opacity.
    pressed {
      animate {
        foreground(colorScheme.onSurface.copy(alpha = 0.1f))
      }
    }
  }

  /** Tappable row on the settings screen. */
  val settingsRowStyle: Style = Style {
    shape(RoundedCornerShape(size = 8.dp))
    clip()
    contentPaddingVertical(16.dp)
    pressed {
      animate {
        foreground(colorScheme.onSurface.copy(alpha = 0.1f))
      }
    }
  }

  /** Username chip in the note list toolbar; dimmed while disconnected. */
  val toolbarButtonStyle: Style = Style {
    shape(shapes.extraSmall)
    clip()
    background(colorScheme.primary)
    contentPadding(4.dp)
    disconnected {
      background(colorScheme.primary.copy(alpha = 0.5f))
    }
  }

  /** Top app bar surface; window-inset padding stays on the caller's modifier. */
  val toolbarStyle: Style = Style {
    background(colorScheme.surfaceContainerLowest)
  }

  /** Card hosted inside a [androidx.compose.ui.window.Dialog]. */
  val dialogStyle: Style = Style {
    shape(shapes.medium)
    clip()
    border(0.5.dp, colorScheme.onSurfaceVariant)
    background(colorScheme.surfaceContainerLowest)
    contentPadding(24.dp)
  }

  /** Container for a row of mutually exclusive icon buttons. */
  val segmentedControlStyle: Style = Style {
    shape(shapes.medium)
    clip()
    background(colorScheme.surfaceVariant)
  }

  /** A single icon button inside [segmentedControlStyle]. */
  val segmentedControlButtonStyle: Style = Style {
    shape(shapes.medium)
    clip()
    width(56.dp)
    height(56.dp)
  }

  /** 1dp horizontal hairline. */
  val dividerStyle: Style = Style {
    fillWidth()
    height(1.dp)
    background(colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
  }

  /** Higher-contrast divider used between settings rows. */
  val settingsDividerStyle: Style = Style {
    background(colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
  }
}
