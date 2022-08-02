package de.thm.mow2.fleetbattlegameandroid.controller.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.model.helper.GridHelperObject
import de.thm.mow2.fleetbattlegameandroid.controller.helper.SquareCell

class FieldAdapter(
    context: Context,
    private val onClick: ((GridHelperObject) -> Unit)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var cells = ArrayList<GridHelperObject>()
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UiCellViewHolder(
            inflater.inflate(R.layout.recyclerview_game_grid_item, parent, false),
            onClick
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as UiCellViewHolder
        holder.bind(cells[position])
    }

    fun updateList(newList: List<GridHelperObject>) {
        cells.clear()
        cells.addAll(newList)
    }

    override fun getItemCount(): Int {
        return cells.size
    }

    class UiCellViewHolder(
        itemView: View,
        onClick: (GridHelperObject) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val view = itemView as SquareCell
        private var cell: GridHelperObject? = null

        init {
            view.setOnClickListener {
                cell?.let(onClick)
                it.invalidate()
            }
        }

        fun bind(cell: GridHelperObject) {
            this.cell = cell
            view.bind(cell)
        }
    }
}