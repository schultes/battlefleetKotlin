package de.thm.mow2.fleetbattlegameandroid.controller.helper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleField
import de.thm.mow2.fleetbattlegameandroid.model.grid.Position
import de.thm.mow2.fleetbattlegameandroid.model.ship.Orientation

class AdvancedSquareCell : View {
    private var cell: FleetBattleField? = null
    private var isItOwnGrid = false
    private var lastClicked: Position? = null

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

        val rect = Rect(0, 0, width, height)

        var shipImage: Drawable? = null
        var effectImage: Drawable? = null

        var onTop: Drawable? = null

        if (isItOwnGrid) {
            cell?.ship?.let { shipObject ->
                if (!shipObject.submerged) {
                    effectImage =
                        if (cell!!.isVisible) AppCompatResources.getDrawable(context, R.drawable.game_explosion) else null
                    if(shipObject.length == 1){
                        shipImage = AppCompatResources.getDrawable(context, R.drawable.ship_single)
                    }
                    else if (cell!!.position.row == shipObject.startPosition.row && cell!!.position.column == shipObject.startPosition.column) {
                        shipImage = AppCompatResources.getDrawable(context, R.drawable.ship_end)
                        if (shipObject.orientation == Orientation.HORIZONTAL) {
                            canvas.rotate(180f, width / 2f, height / 2f)
                        } else {
                            canvas.rotate(270f, width / 2f, height / 2f)
                        }
                    } else if (cell!!.position.row == shipObject.getLastPositionOfShip().row && cell!!.position.column == shipObject.getLastPositionOfShip().column) {
                        shipImage = AppCompatResources.getDrawable(context, R.drawable.ship_end)
                        if (shipObject.orientation == Orientation.HORIZONTAL) {
                            canvas.rotate(0f, width / 2f, height / 2f)
                        } else {
                            canvas.rotate(90f, width / 2f, height / 2f)
                        }
                    } else {
                        shipImage = AppCompatResources.getDrawable(context, R.drawable.ship_middle)
                    }
                } else if (shipObject.submerged) {
                    shipImage = AppCompatResources.getDrawable(context, R.drawable.game_skull)
                } else {
                    effectImage =
                        if (cell!!.isVisible) AppCompatResources.getDrawable(context, R.drawable.game_explosion) else null
                }
            } ?: run {
                cell?.let { field ->
                    effectImage =
                        if (field.isVisible) AppCompatResources.getDrawable(context, R.drawable.game_watersplash) else null
                }
            }
        } else {
            cell?.let { field ->
                // Already shot field
                if (field.isVisible) {
                    cell!!.ship?.let { shipObject ->
                        // Found ship (submerged or just a hit)
                        effectImage =
                            if (shipObject.submerged)
                                AppCompatResources.getDrawable(context, R.drawable.game_skull)
                            else
                                AppCompatResources.getDrawable(context, R.drawable.game_explosion)
                    } ?: run {
                        // Found field without ship (water)
                        effectImage = AppCompatResources.getDrawable(context, R.drawable.game_watersplash)
                    }

                }
            }
        }

        if (lastClicked != null && lastClicked == cell?.position) {
            onTop = AppCompatResources.getDrawable(context, R.drawable.game_crosshair)
        }

        canvas.save()

        val paint = Paint()
        paint.style = Paint.Style.FILL
        val color = String.format(
            "#E6%06x",
            ContextCompat.getColor(context, R.color.primary_normal) and 0xffffff
        )
        paint.color = Color.parseColor(color)
        canvas.drawRect(rect, paint)

        shipImage?.let {
            it.bounds = rect
            it.draw(canvas)
        }
        effectImage?.let {
            it.bounds = rect
            it.draw(canvas)
        }
        onTop?.let {
            it.bounds = rect
            it.draw(canvas)
        }

        // border
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 3.0F
        canvas.drawRect(rect, paint)
    }

    fun bind(cell: FleetBattleField, isItOwnGrid: Boolean, lastClicked: Position?) {
        this.cell = cell
        this.isItOwnGrid = isItOwnGrid
        this.lastClicked = lastClicked
        invalidate()
    }
}