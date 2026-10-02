package com.dhimandasgupta.notemark.ui.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.StyleStateKey
import androidx.compose.foundation.style.animate
import androidx.compose.foundation.style.border
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.contentPaddingVertical
import androidx.compose.foundation.style.fillWidth
import androidx.compose.foundation.style.pressed
import androidx.compose.foundation.style.state
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
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
