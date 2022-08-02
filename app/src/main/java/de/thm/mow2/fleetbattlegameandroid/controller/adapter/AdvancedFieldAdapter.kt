package de.thm.mow2.fleetbattlegameandroid.controller.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleField
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGrid
import de.thm.mow2.fleetbattlegameandroid.model.grid.Field
import de.thm.mow2.fleetbattlegameandroid.model.grid.Position
import de.thm.mow2.fleetbattlegameandroid.model.service.AuthenticationService
import de.thm.mow2.fleetbattlegameandroid.controller.helper.AdvancedSquareCell

class AdvancedFieldAdapter(
    context: Context,
    private var grid: FleetBattleGrid,
    private var lastClicked: Position?,
    private val onClick: ((Field) -> Unit)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)
    private val username = AuthenticationService.getUsername()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UiCellViewHolder(
            inflater.inflate(R.layout.recyclerview_game_field_item, parent, false),
            onClick
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as UiCellViewHolder
        val ownGrid = grid.ownerUsername == username

        holder.bind(grid.getField(get2dPosition(position)) as FleetBattleField, ownGrid, lastClicked)

    }

    override fun getItemCount(): Int {
        return grid.width * grid.width
    }

    fun updateGrid(grid: FleetBattleGrid) {
        this.grid = grid
        notifyDataSetChanged()
    }

    fun showMove(position: Position?) {
        this.lastClicked = position
        notifyDataSetChanged()
    }

    private fun get2dPosition(position: Int): Position {
        val x = position % grid.width
        val y = position / grid.width

        return Position(y, x)
    }

    class UiCellViewHolder(
        itemView: View,
        onClick: (Field) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val view = itemView as AdvancedSquareCell
        private var cell: Field? = null
        private var isOwnGrid = false
        private var lastClicked: Position? = null

        init {
            view.setOnClickListener { it ->
                cell?.let(onClick)
                it.invalidate()
            }
        }

        fun bind(cell: FleetBattleField, isOwnGrid: Boolean, lastClicked: Position?) {
            this.cell = cell
            this.isOwnGrid = isOwnGrid
            this.lastClicked = lastClicked
            view.bind(cell, isOwnGrid, lastClicked)
        }
    }
}