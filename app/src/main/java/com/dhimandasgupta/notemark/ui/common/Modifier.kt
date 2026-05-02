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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.WindowInsetsRulers
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.apply
import kotlin.math.roundToInt

/**
 * A lifecycle-aware and debounced clickable modifier.
 *
 * Clicks are only processed if:
 * 1. The Composable's lifecycle is at least in the RESUMED state (configurable).
 * 2. Enough time (debounceIntervalMs) has passed since the last processed click.
 *
 * @param activeState The minimum Lifecycle.State required for the click to be processed. Defaults to RESUME.
 * @param interactionSource The [MutableInteractionSource] for this clickable.
 * @param enabled Global enabled state for the clickable. Overrides lifecycle check if false.
 * @param onClickLabel Semantic / accessibility label for the onClick action.
 * @param role Semantic / accessibility role.
 * @param debounceIntervalMs Time in milliseconds for debounce.
 * @param onClick The action to perform.
 */
@Composable
fun Modifier.lifecycleAwareDebouncedClickable(
    activeState: Lifecycle.State = Lifecycle.State.RESUMED,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    debounceIntervalMs: Long = 700L, // Default debounce interval
    onClick: () -> Unit
): Modifier = composed {
    val owner = LocalLifecycleOwner.current

    val currentOnClick by rememberUpdatedState(newValue = onClick)
    var lastClickTime by remember { mutableLongStateOf(value = 0L) }
    var lifecycleAllowsClick by remember { mutableStateOf(value = owner.lifecycle.currentState.isAtLeast(activeState)) }

    DisposableEffect(key1 = owner, key2 = activeState) {
        val observer = LifecycleEventObserver { _, _ ->
            lifecycleAllowsClick = owner.lifecycle.currentState.isAtLeast(activeState)
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
        }
    }

    val isClickEnabled = enabled && lifecycleAllowsClick

    clickable(
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

/**
 * Aligns the composable to the safe drawing insets, effectively avoiding system UI elements
 * like status bars and navigation bars.
 *
 * This modifier uses a `layout` modifier to measure the composable with the original constraints
 * and then places it by offsetting its y-position downwards by the top safe drawing inset.
 * The width and height of the composable remain as measured. This is useful for positioning
 * content below the status bar.
 *
 * It is important to note that this only accounts for the `top` inset. For more complex
 * scenarios involving other insets (e.g., `bottom`, `start`, `end`), a more comprehensive
 * window insets handling approach should be used, such as the accompanist-insets library or
 * the official `WindowInsets` APIs in Compose.
 */
@Composable
fun Modifier.alignToSafeDrawing(): Modifier {
    return layout { measurable, constraints ->
        if (constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
            val placeable = measurable.measure(constraints)
            val width = placeable.width
            val height = placeable.height
            layout(width, height) {
                val bottom = WindowInsetsRulers.SafeDrawing.current.bottom
                    .current(defaultValue = 0f).roundToInt() - height
                val right = WindowInsetsRulers.SafeDrawing.current.right
                    .current(defaultValue = 0f).roundToInt()
                val left = WindowInsetsRulers.SafeDrawing.current.left
                    .current(defaultValue = 0f).roundToInt()
                measurable.measure(
                    constraints = Constraints.fixed(
                        width = right - left,
                        height = height
                    )
                ).place(x = left, y = bottom)
            }
        } else {
            val placeable = measurable.measure(constraints)
            layout(width = placeable.width, height = placeable.height) {
                placeable.place(x = 0, y = 0)
            }
        }
    }
}

/**
 * Draws a grid pattern on the background of the composable.
 *
 * This modifier uses [drawWithCache] to efficiently cache the grid path and re-draws it
 * over the existing content. The grid consists of vertical and horizontal lines spaced
 * according to the provided [gridSize].
 *
 * @param gridColor The color of the grid lines. Defaults to [Color.Red].
 * @param gridSize The size of each grid square in density-independent pixels (dp). Defaults to 10.
 */
@Composable
fun Modifier.drawBackgroundGrid(
    gridColor: Color = Color.Red,
    gridSize: Int = 10 // Default grid size
): Modifier = drawWithCache {
    val stepSizePx = gridSize.dp.toPx().roundToInt().toFloat()

    val strokeWidth = 1f
    val offset = strokeWidth / 2f

    val gridPath = Path().apply {
        // Vertical lines
        var x = 0f
        while (x <= size.width) {
            moveTo(x + offset, 0f)
            lineTo(x + offset, size.height)
            x += stepSizePx
        }
        // Horizontal lines
        var y = 0f
        while (y <= size.height) {
            moveTo(0f, y + offset)
            lineTo(size.width, y + offset)
            y += stepSizePx
        }
    }

    onDrawWithContent {
        drawContent()
        drawPath(
            path = gridPath,
            color = gridColor,
            style = Stroke(width = strokeWidth)
        )
    }
}