package com.uniplan.app.ui.jar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.BounceInterpolator
import com.uniplan.app.data.local.db.CoinEntity
import com.uniplan.app.domain.model.CoinTier
import com.uniplan.app.domain.usecase.JarLayout

class CoinJarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val jarOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5")
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val jarHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#40FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val coinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val coinStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val coinIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        textSize = 24f
    }

    private var goal: Int = 30
    private var coins: List<CoinEntity> = emptyList()
    private var coinPositions: List<PointF> = emptyList()
    private var animatedHeights: MutableMap<Int, Float> = mutableMapOf()

    fun setCoins(coinList: List<CoinEntity>, monthlyGoal: Int) {
        this.coins = coinList
        this.goal = monthlyGoal
        requestLayout()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        recalculateLayout()
    }

    private fun recalculateLayout() {
        if (width == 0 || height == 0 || coins.isEmpty()) return

        val jarW = width * 0.8f
        val jarH = height * 0.8f
        val radius = JarLayout.calculateRadius(jarW, jarH, goal)
        val layout = JarLayout(jarW, jarH, radius)

        val posList = mutableListOf<PointF>()
        val startX = (width - jarW) / 2f
        val startY = height.toFloat() - (height - jarH) / 2f

        coins.forEachIndexed { index, coin ->
            val p = layout.place(coin)
            val screenX = startX + p.x
            val screenY = startY - p.y
            posList.add(PointF(screenX, screenY))

            if (!coin.isSeen && !animatedHeights.containsKey(index)) {
                animateCoinDrop(index, screenY - 200f, screenY)
            } else {
                animatedHeights[index] = screenY
            }
        }
        coinPositions = posList
    }

    private fun animateCoinDrop(index: Int, startY: Float, endY: Float) {
        ValueAnimator.ofFloat(startY, endY).apply {
            duration = 500
            interpolator = BounceInterpolator()
            addUpdateListener { anim ->
                animatedHeights[index] = anim.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        val jarW = w * 0.8f
        val jarH = h * 0.8f
        val left = (w - jarW) / 2f
        val top = (h - jarH) / 2f
        val right = left + jarW
        val bottom = top + jarH

        // Draw glass jar path
        val path = Path().apply {
            moveTo(left + jarW * 0.15f, top)
            lineTo(right - jarW * 0.15f, top)
            lineTo(right - jarW * 0.15f, top + jarH * 0.08f)
            lineTo(right, top + jarH * 0.15f)
            lineTo(right, bottom - 24f)
            arcTo(RectF(right - 48f, bottom - 48f, right, bottom), 0f, 90f)
            lineTo(left + 24f, bottom)
            arcTo(RectF(left, bottom - 48f, left + 48f, bottom), 90f, 90f)
            lineTo(left, top + jarH * 0.15f)
            lineTo(left + jarW * 0.15f, top + jarH * 0.08f)
            close()
        }

        canvas.drawPath(path, jarOutlinePaint)

        // Draw highlight
        val highlightPath = Path().apply {
            moveTo(left + 16f, top + jarH * 0.15f)
            lineTo(left + 16f, bottom - 32f)
        }
        canvas.drawPath(highlightPath, jarHighlightPaint)

        // Draw Coins
        coins.forEachIndexed { i, coin ->
            val pos = coinPositions.getOrNull(i) ?: return@forEachIndexed
            val currentY = animatedHeights[i] ?: pos.y
            val r = if (coin.tier == CoinTier.FOCUS) 20f else 24f

            val colorHex = when (coin.tier) {
                CoinTier.BRONZE -> "#B87333"
                CoinTier.SILVER -> "#C0C0C0"
                CoinTier.GOLD -> "#FFC107"
                CoinTier.FOCUS -> "#4FC3F7"
            }
            val strokeHex = when (coin.tier) {
                CoinTier.BRONZE -> "#8B4513"
                CoinTier.SILVER -> "#9E9E9E"
                CoinTier.GOLD -> "#FF8F00"
                CoinTier.FOCUS -> "#0288D1"
            }

            coinPaint.color = Color.parseColor(colorHex)
            coinStrokePaint.color = Color.parseColor(strokeHex)

            canvas.drawCircle(pos.x, currentY, r, coinPaint)
            canvas.drawCircle(pos.x, currentY, r, coinStrokePaint)

            if (coin.tier == CoinTier.GOLD) {
                canvas.drawText("★", pos.x - 10f, currentY + 8f, coinIconPaint)
            }
        }
    }
}
