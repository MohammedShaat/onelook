package com.example.onelook.tasks.presentation.home.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.onelook.R
import kotlin.math.min

class CustomSupplementProgressView(context: Context, attrs: AttributeSet) :
    View(context, attrs) {

    var total: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var progress: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    private var ringColor: Int
    private var progressColor: Int
    private var textColor: Int

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomProgressView, 0, 0)
            .apply {
                try {
                    total = getInteger(R.styleable.CustomProgressView_total, 0)
                    progress = getInteger(R.styleable.CustomProgressView_progress, 0)
                    ringColor = getColor(
                        R.styleable.CustomProgressView_ringColor,
                        ResourcesCompat.getColor(resources, R.color.light_grey, null)
                    )
                    progressColor = getColor(
                        R.styleable.CustomProgressView_progressColor,
                        ResourcesCompat.getColor(resources, R.color.turquoise, null)
                    )
                    textColor = getColor(
                        R.styleable.CustomProgressView_textColor,
                        ResourcesCompat.getColor(resources, R.color.black, null)
                    )
                } finally {
                    recycle()
                }
            }
    }

    private var w = 0f
    private var h = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var space = 0f
    private var strokeWidth = 0f
    private var ringRadius = 0f

    private val paint = Paint()
    private lateinit var frame: RectF


    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)
        this.w = width.toFloat()
        this.h = height.toFloat()
        centerX = w / 2
        centerY = h / 2
        space = min(w, h)
        strokeWidth = 10 * space / 100
        ringRadius = (space - strokeWidth) / 2
        frame = RectF(0f, 0f, h, w)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//        drawFrameTesting(canvas)
        drawRing(canvas)
        drawProgressArc(canvas)
        drawProgressText(canvas)
    }

    private fun drawFrameTesting(canvas: Canvas) {
        paint.apply {
            style = Paint.Style.FILL
            color = ResourcesCompat.getColor(resources, android.R.color.holo_red_light, null)
        }
        canvas.drawRect(frame, paint)
    }

    private fun drawRing(canvas: Canvas) {
        paint.apply {
            style = Paint.Style.STROKE
            color = ringColor
            strokeWidth = this@CustomSupplementProgressView.strokeWidth
        }
        canvas.drawCircle(centerX, centerY, ringRadius, paint)
    }

    private fun drawProgressArc(canvas: Canvas) {
        paint.apply {
            color = progressColor
            strokeCap = Paint.Cap.ROUND
        }
        val decimalProgress = if (total > 0) progress / total.toFloat() else 0f
        canvas.drawArc(
            0f + strokeWidth / 2, 0f + strokeWidth / 2,
            w - strokeWidth / 2, h - strokeWidth / 2,
            -90f, 360 * decimalProgress, false, paint
        )
    }

    private fun drawProgressText(canvas: Canvas) {
        paint.apply {
            style = Paint.Style.FILL
            color = textColor
            textSize = ringRadius * .7f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$progress/$total", centerX, centerY + (paint.textSize * .35f), paint)
    }
}