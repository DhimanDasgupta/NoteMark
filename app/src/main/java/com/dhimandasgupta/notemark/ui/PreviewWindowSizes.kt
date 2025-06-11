@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package com.dhimandasgupta.notemark.ui

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Preview(name = "Compact Phone Portrait", widthDp = 360, heightDp = 780, showBackground = true)
@Preview(name = "Compact Phone Landscape", widthDp = 780, heightDp = 360, showBackground = true)
@Preview(name = "Medium Tablet/ Foldable Portrait", widthDp = 600, heightDp = 960, showBackground = true)
@Preview(name = "Medium Tablet/ Foldable Landscape", widthDp = 960, heightDp = 600, showBackground = true)
@Preview(name = "Expanded Tablet Portrait", widthDp = 840, heightDp = 1280, showBackground = true)
@Preview(name = "Expanded Tablet Landscape", widthDp = 1280, heightDp = 840, showBackground = true)
annotation class WindowSizePreviews

@Preview(name = "Phone Portrait", widthDp = 360, heightDp = 780, showBackground = true)
annotation class PhonePortraitPreview

@Preview(name = "Phone Landscape", widthDp = 780, heightDp = 360, showBackground = true)
annotation class PhoneLandscapePreview

@Preview(name = "Tablet (Medium) Portrait", widthDp = 600, heightDp = 900, showBackground = true)
annotation class TabletMediumPortraitPreview

@Preview(name = "Tablet (Medium) Landscape", widthDp = 900, heightDp = 600, showBackground = true)
annotation class TabletMediumLandscapePreview

@Preview(name = "Tablet (Expanded) Portrait", widthDp = 1280, heightDp = 800, showBackground = true)
annotation class TabletExpandedPortraitPreview

@Preview(name = "Tablet (Expanded) Portrait", widthDp = 800, heightDp = 1280, showBackground = true)
annotation class TabletExpandedLandscapePreview

val phonePortrait = WindowSizeClass.calculateFromSize(
    size = DpSize(
        width = 360.dp,
        height = 780.dp
    )
)

val phoneLandscape = WindowSizeClass.calculateFromSize(
    size = DpSize(
        width = 780.dp,
        height = 360.dp
    )
)

val mediumTabletPortrait = WindowSizeClass.calculateFromSize(
    size = DpSize(
        width = 600.dp,
        height = 900.dp
    )
)

val mediumTabletLandscape = WindowSizeClass.calculateFromSize(
    size = DpSize(
        width = 900.dp,
        height = 600.dp
    )
)

val extendedTabletPortrait = WindowSizeClass.calculateFromSize(
    size = DpSize(
        width = 800.dp,
        height = 1280.dp
    )
)

val extendedTabletLandscape = WindowSizeClass.calculateFromSize(
    size = DpSize(
        width = 1280.dp,
        height = 800.dp
    )
)
