package com.dhimandasgupta.notemark.util

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import com.dhimandasgupta.notemark.ui.designsystem.compose.RecompositionCountKey

fun SemanticsNodeInteraction.assertRecompositionCount(expected: Int): SemanticsNodeInteraction {
  assert(
    matcher =
      SemanticsMatcher.expectValue(
        key = RecompositionCountKey,
        expectedValue = expected,
      )
  )
  return this
}
