package com.dhimandasgupta.notemark.ui.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

val LightColorScheme =
  lightColorScheme(
    primary = Primary,
    surface = Surface,
    surfaceContainerLowest = SurfaceLowest,
    background = Background,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryDark,
    surface = SurfaceDark,
    surfaceContainerLowest = SurfaceLowestDark,
    background = BackgroundDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorDark,
  )

val Shapes =
  Shapes(
    extraSmall = RoundedCornerShape(5.dp),
    medium = RoundedCornerShape(15.dp),
  )

/**
 * Theme tokens readable from a [androidx.compose.foundation.style.StyleScope]. Material 3 keeps its
 * own composition locals internal, so the active scheme is re-provided here for Styles to consume.
 */
@Immutable
class NoteMarkThemeTokens(
  val colorScheme: ColorScheme,
  val shapes: Shapes,
)

val LocalNoteMarkTheme = staticCompositionLocalOf {
  NoteMarkThemeTokens(colorScheme = LightColorScheme, shapes = Shapes)
}

object NoteMarkTheme {
  val styles: NoteMarkStyles = NoteMarkStyles
}

@Composable
fun NoteMarkTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  // Ignoring colorScheme
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val tokens =
    remember(colorScheme) { NoteMarkThemeTokens(colorScheme = colorScheme, shapes = Shapes) }

  CompositionLocalProvider(LocalNoteMarkTheme provides tokens) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      shapes = Shapes,
      content = content,
    )
  }
}
