package com.uniplan.app.ui.coinjar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.uniplan.app.data.local.db.CoinEntity
import com.uniplan.app.domain.model.CoinTier
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

data class PlacedCoin(
    val coin: CoinEntity,
    val center: Offset,
    val radius: Float
)

class JarLayout(
    val innerW: Float,
    val innerH: Float,
    val baseR: Float
) {
    private val step = max(1f, baseR / 2f)
    private val cols = ceil(innerW / step).toInt() + 1
    private val heights = FloatArray(cols)

    fun place(coin: CoinEntity): PlacedCoin {
        val rr = if (coin.tier == CoinTier.FOCUS) baseR * 0.8f else baseR
        val cx = rr + Random(coin.seed).nextFloat() * max(1f, innerW - 2 * rr)
        val from = max(0, floor((cx - rr) / step).toInt())
        val to = min(cols - 1, ceil((cx + rr) / step).toInt())

        var cy = rr
        for (j in from..to) {
            val dx = j * step - cx
            val dy = sqrt(max(0f, rr * rr - dx * dx))
            cy = max(cy, heights[j] + dy)
        }
        for (j in from..to) {
            val dx = j * step - cx
            val dy = sqrt(max(0f, rr * rr - dx * dx))
            heights[j] = max(heights[j], cy + dy)
        }

        return PlacedCoin(coin, Offset(cx, cy), rr)
    }
}

@Composable
fun CoinJarCanvas(
    coins: List<CoinEntity>,
    goal: Int,
    onCoinClicked: (CoinEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f) // 3:4 aspect ratio
            .pointerInput(coins, goal) {
                detectTapGestures { tapOffset ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()

                    val innerW = w * 0.8f
                    val innerH = h * 0.78f
                    val innerLeft = w * 0.1f
                    val innerBottom = h * 0.95f

                    val area = innerW * innerH
                    var r = sqrt((0.85f * 0.7f * area) / (Math.PI.toFloat() * max(1, goal)))
                    r = r.coerceIn(innerW / 30f, innerW / 6f)

                    val layout = JarLayout(innerW, innerH, r)
                    val placedList = coins.map { layout.place(it) }

                    // Find tapped coin (from top to bottom in visual z-order)
                    val tapped = placedList.reversed().find { p ->
                        val screenCx = innerLeft + p.center.x
                        val screenCy = innerBottom - p.center.y
                        val distSq = (tapOffset.x - screenCx) * (tapOffset.x - screenCx) +
                                (tapOffset.y - screenCy) * (tapOffset.y - screenCy)
                        distSq <= p.radius * p.radius
                    }

                    tapped?.let { onCoinClicked(it.coin) }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // 1. Draw Glass Jar
        val jarPath = Path().apply {
            val left = w * 0.1f
            val right = w * 0.9f
            val top = h * 0.15f
            val bottom = h * 0.95f
            val neckLeft = w * 0.2f
            val neckRight = w * 0.8f
            val neckTop = h * 0.05f

            moveTo(neckLeft, neckTop)
            lineTo(neckRight, neckTop)
            lineTo(neckRight, top)
            lineTo(right, top)
            lineTo(right, bottom - 20f)
            quadraticTo(right, bottom, right - 20f, bottom)
            lineTo(left + 20f, bottom)
            quadraticTo(left, bottom, left, bottom - 20f)
            lineTo(left, top)
            lineTo(neckLeft, top)
            close()
        }

        // Jar outline
        drawPath(
            path = jarPath,
            color = Color.DarkGray.copy(alpha = 0.8f),
            style = Stroke(width = 6f)
        )

        // Glass highlight reflection
        drawRoundRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(w * 0.15f, h * 0.2f),
            size = Size(12f, h * 0.65f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // 2. Draw Coins
        val innerW = w * 0.8f
        val innerH = h * 0.78f
        val innerLeft = w * 0.1f
        val innerBottom = h * 0.95f

        val area = innerW * innerH
        var r = sqrt((0.85f * 0.7f * area) / (Math.PI.toFloat() * max(1, goal)))
        r = r.coerceIn(innerW / 30f, innerW / 6f)

        val layout = JarLayout(innerW, innerH, r)
        val placedList = coins.map { layout.place(it) }

        for (p in placedList) {
            val cx = innerLeft + p.center.x
            val cy = innerBottom - p.center.y

            val (faceColor, borderColor) = when (p.coin.tier) {
                CoinTier.BRONZE -> Color(0xFFB87333) to Color(0xFF8B5A2B)
                CoinTier.SILVER -> Color(0xFFC0C0C0) to Color(0xFF808080)
                CoinTier.GOLD -> Color(0xFFFFC107) to Color(0xFFC79100)
                CoinTier.FOCUS -> Color(0xFF4FC3F7) to Color(0xFF0288D1)
            }

            // Coin circle
            drawCircle(
                color = faceColor,
                radius = p.radius,
                center = Offset(cx, cy)
            )

            // Coin border
            drawCircle(
                color = borderColor,
                radius = p.radius,
                center = Offset(cx, cy),
                style = Stroke(width = 3f)
            )

            // Highlight arc
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = p.radius * 0.6f,
                center = Offset(cx - p.radius * 0.2f, cy - p.radius * 0.2f)
            )
        }
    }
}
