package com.matrixaiopro.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

data class PathData(
    val path: Path,
    val color: Color,
    val strokeWidth: Float = 10f
)

@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    selectedColor: Color = Color.Cyan
) {
    val paths = remember { mutableStateListOf<PathData>() }
    val currentPath = remember { mutableStateOf<Path?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val newPath = Path().apply {
                            moveTo(offset.x, offset.y)
                        }
                        currentPath.value = newPath
                        paths.add(PathData(newPath, selectedColor))
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentPath.value?.lineTo(change.position.x, change.position.y)
                        // Trigger recomposition by re-adding or updating the state
                        // Actually mutableStateListOf handles this if we update the path itself?
                        // Path is not a state, so we might need a workaround or just redraw.
                        // Compose Canvas redraws on any state change.
                        val last = paths.lastOrNull()
                        if (last != null) {
                            paths[paths.size - 1] = last.copy() // Force update
                        }
                    },
                    onDragEnd = {
                        currentPath.value = null
                    }
                )
            }
    ) {
        paths.forEach { pathData ->
            drawPath(
                path = pathData.path,
                color = pathData.color,
                style = Stroke(
                    width = pathData.strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

// Internal state management for Canvas recomposition
private fun <T> androidx.compose.runtime.MutableState<T>.trigger() {
    val temp = this.value
    this.value = temp
}

import androidx.compose.runtime.mutableStateOf
