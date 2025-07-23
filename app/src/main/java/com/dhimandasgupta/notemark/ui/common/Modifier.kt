package com.dhimandasgupta.notemark.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * A lifecycle-aware and debounced clickable modifier.
 *
 * Clicks are only processed if:
 * 1. The Composable's lifecycle is at least in the RESUMED state (configurable).
 * 2. Enough time (debounceIntervalMs) has passed since the last processed click.
 *
 * @param owner The LifecycleOwner to observe. Defaults to LocalLifecycleOwner.current.
 * @param activeState The minimum Lifecycle.State required for the click to be processed. Defaults to RESUMED.
 * @param interactionSource The [MutableInteractionSource] for this clickable.
 * @param enabled Global enabled state for the clickable. Overrides lifecycle check if false.
 * @param onClickLabel Semantic / accessibility label for the onClick action.
 * @param role Semantic / accessibility role.
 * @param debounceIntervalMs Time in milliseconds for debounce.
 * @param onClick The action to perform.
 */
@Composable
fun Modifier.lifecycleAwareDebouncedClickable(
    owner: LifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current,
    activeState: Lifecycle.State = Lifecycle.State.RESUMED,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    debounceIntervalMs: Long = 700L, // Default debounce interval
    onClick: () -> Unit
): Modifier = composed {
    val currentOnClick by rememberUpdatedState(newValue = onClick)
    var lastClickTime by remember { mutableLongStateOf(value = 0L) }
    var lifecycleAllowsClick by remember { mutableStateOf(value = owner.lifecycle.currentState.isAtLeast(activeState)) }

    DisposableEffect(key1 = owner, key2 = activeState) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycleAllowsClick = owner.lifecycle.currentState.isAtLeast(activeState)
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
        }
    }

    val isClickEnabled = enabled && lifecycleAllowsClick

    this.clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = isClickEnabled, // Combined enabled state
        onClickLabel = onClickLabel,
        role = role
    ) {
        if (isClickEnabled) { // Double check here, though clickable's enabled should handle it
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= debounceIntervalMs) {
                lastClickTime = currentTime
                currentOnClick()
            }
        }
    }
}