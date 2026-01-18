package com.snakegame.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.snakegame.domain.model.Fruit

/**
 * Renders a fruit on the game grid using Compose Canvas.
 *
 * Visual specifications:
 * - Shape: Circle (distinct from rectangular snake segments)
 * - Color: Red (high contrast with snake and background)
 * - Size: 40% of cell size (fits comfortably within grid cell)
 * - Position: Centered within grid cell
 *
 * Performance: Direct Canvas API, no bitmap overhead, scalable across screen sizes.
 *
 * @param fruit The fruit to render (null if no fruit present)
 * @param cellSize Size of one grid cell in DP
 * @param modifier Modifier for the Canvas
 */
@Composable
fun FruitRenderer(
    fruit: Fruit?,
    cellSize: Dp,
    modifier: Modifier = Modifier
) {
    if (fruit != null && fruit.isActive) {
        Canvas(modifier = modifier) {
            val cellSizePx = cellSize.toPx()

            // Calculate fruit center position
            val fruitX = fruit.position.x * cellSizePx + cellSizePx / 2
            val fruitY = fruit.position.y * cellSizePx + cellSizePx / 2

            // Draw circle: 40% of cell size for comfortable fit
            val radius = cellSizePx * 0.4f

            drawCircle(
                color = Color.Red,
                radius = radius,
                center = Offset(fruitX, fruitY)
            )
        }
    }
}
