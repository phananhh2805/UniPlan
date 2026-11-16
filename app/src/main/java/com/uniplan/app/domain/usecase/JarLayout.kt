package com.uniplan.app.domain.usecase

import android.graphics.PointF
import com.uniplan.app.data.local.db.CoinEntity
import com.uniplan.app.domain.model.CoinTier
import java.util.Random
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class JarLayout(
    val innerW: Float,
    val innerH: Float,
    val r: Float
) {
    private val step = r / 2f
    private val cols = ceil(innerW / step).toInt() + 1
    private val heights = FloatArray(cols)

    fun place(coin: CoinEntity): PointF {
        val rr = if (coin.tier == CoinTier.FOCUS) r * 0.8f else r
        val random = Random(coin.seed.toLong())
        val cx = rr + random.nextFloat() * max(0.1f, innerW - 2 * rr)

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

        return PointF(cx, cy)
    }

    companion object {
        fun calculateRadius(innerW: Float, innerH: Float, goal: Int): Float {
            val area = innerW * innerH
            val calculated = sqrt(0.85f * 0.7f * area / (Math.PI.toFloat() * max(1, goal)))
            val minR = innerW / 30f
            val maxR = innerW / 6f
            return calculated.coerceIn(minR, maxR)
        }
    }
}
