package de.thm.mow2.fleetbattlegameandroid.controller.helper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color

import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.model.helper.GridHelperObject

class SquareCell : View {
    private var cell: GridHelperObject? = null
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paint = Paint()
        val rect = Rect(0, 0, height, bottom)
        paint.style = Paint.Style.FILL

        cell?.let { cellObject ->
            if (cellObject.isShipSet) {
                paint.color = Color.WHITE
            } else {
                val color = String.format("#E6%06x", ContextCompat.getColor(context, R.color.primary_normal) and 0xffffff)
                paint.color = Color.parseColor(color)
            }
        }
        canvas.drawRect(rect, paint)
        // border
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 3.0F
        canvas.drawRect(rect, paint)
    }

    fun bind(cell: GridHelperObject) {
        this.cell = cell
        invalidate()
    }
}