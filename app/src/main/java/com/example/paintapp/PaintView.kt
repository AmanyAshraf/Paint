package com.example.paintapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


private const val STROKE_WIDTH = 12f

enum class TOOL {
    Pencil,
    Arrow,
    Rectangle,
    Circle,
    ColorPalette
}

class PaintView constructor(context: Context, attributeSet: AttributeSet) :
    View(context, attributeSet) {

    interface PaintController {
        fun onDraw(canvas: Canvas)
        fun touchMove(event: MotionEvent)
        fun touchUp(event: MotionEvent)
        fun touchStart(event: MotionEvent)
        fun setMotionTouchEvent(event: MotionEvent)
        fun drawArrow(
            canvas: Canvas, startX: Float, startY: Float,
            stopX: Float, stopY: Float, paint: Paint
        )

        fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
    }

    private lateinit var paintController: PaintController

    init {
        if (context is PaintController) {
            paintController = context as PaintController
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        paintController.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            paintController.onDraw(it)
        }


    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {

            paintController.setMotionTouchEvent(it)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    paintController.touchStart(it)
                }
                MotionEvent.ACTION_MOVE -> {
                    paintController.touchMove(it)
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    paintController.touchUp(it)
                    invalidate()
                }
            }
        }
        return true
    }




}