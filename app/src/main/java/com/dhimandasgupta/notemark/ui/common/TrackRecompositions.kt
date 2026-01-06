package com.dhimandasgupta.notemark.ui.common

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics

val RecompositionCountKey = SemanticsPropertyKey<Int>(name = "RecompositionCount")

var SemanticsPropertyReceiver.recompositionCount by RecompositionCountKey

fun Modifier.trackRecompositions(): Modifier = composed {
    var recompositions by remember { mutableIntStateOf(0) }

    SideEffect { recompositions++ }

    Modifier.semantics {
        recompositionCount = recompositions
    }
}