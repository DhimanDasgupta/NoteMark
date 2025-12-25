package com.dhimandasgupta.notemark.ui.common

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

// Enum to represent the different layout types
enum class DeviceLayoutType {
    PHONE_PORTRAIT,
    PHONE_LANDSCAPE,
    TABLET_LAYOUT
}

/**
 * Determines the device layout type based on the window size class.
 *
 * @return The DeviceLayoutType (PHONE_PORTRAIT, PHONE_LANDSCAPE, or TABLET_LAYOUT).
 */
@Composable
fun getDeviceLayoutType(): DeviceLayoutType {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)

    return remember(key1 = windowAdaptiveInfo) {
        val isExpandedWidth = windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(widthDpBreakpoint = WIDTH_DP_EXPANDED_LOWER_BOUND)
        val isMediumWidth = windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(widthDpBreakpoint = WIDTH_DP_MEDIUM_LOWER_BOUND)
        val isCompactWidth = !isMediumWidth && !isExpandedWidth

        val isExpandedHeight = windowAdaptiveInfo.windowSizeClass.isHeightAtLeastBreakpoint(heightDpBreakpoint = WIDTH_DP_EXPANDED_LOWER_BOUND)
        val isMediumHeight = windowAdaptiveInfo.windowSizeClass.isHeightAtLeastBreakpoint(heightDpBreakpoint = WIDTH_DP_MEDIUM_LOWER_BOUND)

        when {
            ((isMediumWidth || isExpandedWidth) &&
                    (isMediumHeight || isExpandedHeight)) -> {
                DeviceLayoutType.TABLET_LAYOUT
            }
            else -> {
                if (isCompactWidth) {
                    DeviceLayoutType.PHONE_PORTRAIT
                } else {
                    DeviceLayoutType.PHONE_LANDSCAPE
                }
            }
        }
    }
}