package com.dhimandasgupta.notemark.ui

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Devices.PIXEL
import androidx.compose.ui.tooling.preview.Devices.PIXEL_FOLD
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Compact Phone Portrait/ Light", device = PIXEL, showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "Medium Tablet/ Foldable Portrait/ Light", device = PIXEL_FOLD, showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "Expanded Tablet Portrait/ Light", device = PIXEL_TABLET, showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "Compact Phone Portrait/ Dark", device = PIXEL, showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES, )
@Preview(name = "Medium Tablet/ Foldable Portrait/ Dark", device = PIXEL_FOLD, showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "Expanded Tablet Portrait/ Dark", device = PIXEL_TABLET, showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
annotation class WindowSizePreviews