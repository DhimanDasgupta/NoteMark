package com.dhimandasgupta.notemark.ui.common

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview

// Enum to represent the different layout types
enum class DeviceLayoutType {
    PHONE_PORTRAIT,
    PHONE_LANDSCAPE,
    TABLET_LAYOUT
}

/**
 * Determines the device layout type based on the window size class.
 *
 * @param windowSizeClass The WindowSizeClass calculated for the current window.
 * @return The DeviceLayoutType (PHONE_PORTRAIT, PHONE_LANDSCAPE, or TABLET_LAYOUT).
 */
@Composable
fun getDeviceLayoutType(windowSizeClass: WindowSizeClass): DeviceLayoutType {
    // Remember the calculation to avoid re-computation on every recomposition
    // if windowSizeClass itself is stable.
    return remember(windowSizeClass) {
        val widthSizeClass = windowSizeClass.widthSizeClass
        val heightSizeClass = windowSizeClass.heightSizeClass

        when {
            // Typical tablet heuristic: Medium or Expanded width AND Medium or Expanded height
            (widthSizeClass == WindowWidthSizeClass.Medium || widthSizeClass == WindowWidthSizeClass.Expanded) &&
                    (heightSizeClass == WindowHeightSizeClass.Medium || heightSizeClass == WindowHeightSizeClass.Expanded) -> {
                DeviceLayoutType.TABLET_LAYOUT
            }
            // Typical phone landscape heuristic: Expanded width AND Compact height
            widthSizeClass == WindowWidthSizeClass.Expanded && heightSizeClass == WindowHeightSizeClass.Compact -> {
                DeviceLayoutType.PHONE_LANDSCAPE
            }
            // Typical phone portrait or other compact layouts
            // (Compact width OR Compact height for non-tablets, primarily targeting phone portrait)
            else -> {
                // More refined phone portrait check:
                if (widthSizeClass == WindowWidthSizeClass.Compact && heightSizeClass != WindowHeightSizeClass.Compact) {
                    DeviceLayoutType.PHONE_PORTRAIT
                } else if (widthSizeClass != WindowWidthSizeClass.Compact && heightSizeClass == WindowHeightSizeClass.Compact) {
                    // This could also be phone landscape, but the above case is more specific.
                    // If not caught by tablet or specific phone landscape, could be a wider phone in landscape.
                    DeviceLayoutType.PHONE_LANDSCAPE // Or a more generic phone category if needed
                } else {
                    // Default to phone portrait for other compact scenarios
                    DeviceLayoutType.PHONE_PORTRAIT
                }
            }
        }
    }
}

@Preview(name = "Phone Portrait", widthDp = 360, heightDp = 780, showBackground = true)
annotation class PhonePortraitPreview

@Preview(name = "Phone Landscape", widthDp = 780, heightDp = 360, showBackground = true)
annotation class PhoneLandscapePreview

@Preview(name = "Tablet (Medium)", widthDp = 600, heightDp = 900, showBackground = true)
annotation class TabletMediumPreview

@Preview(name = "Tablet (Expanded)", widthDp = 1280, heightDp = 800, showBackground = true)
annotation class TabletExpandedPreview