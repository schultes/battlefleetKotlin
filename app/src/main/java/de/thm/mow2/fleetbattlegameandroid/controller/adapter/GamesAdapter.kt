package de.thm.mow2.fleetbattlegameandroid.controller.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.service.AuthenticationService
import de.thm.mow2.fleetbattlegameandroid.model.service.DatabaseService
import de.thm.mow2.fleetbattlegameandroid.model.service.GameService
import de.thm.mow2.fleetbattlegameandroid.controller.helper.DateTimeHelper
import kotlinx.android.synthetic.main.recyclerview_list_game_item.view.*

class GamesAdapter(
    private val listType: Int, private val showDelete: Boolean, private val onClick: ((FleetBattleGame) -> Unit)
) : RecyclerView.Adapter<GamesAdapter.GamesViewHolder>() {

    var gameList = ArrayList<FleetBattleGame>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_list_game_item, parent, false)

        return GamesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GamesViewHolder, position: Int) {
        val currentItem = gameList[position]

        if (!showDelete) holder.deleteButton.visibility = View.GONE

        if (showDelete) holder.deleteButton.setOnClickListener {
            val builder = AlertDialog.Builder(holder.imageView.context)

            builder.setMessage("Möchtest du das Spiel wirklich löschen?")
                .setPositiveButton("Ja") { _, _ ->
                    // Delete Game
                    DatabaseService.deleteGameById(currentItem.documentId!!)
                    Toast.makeText(holder.imageView.context, "Das Spiel wurde gelöscht.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Nein") { _, _ ->
                    // Do nothing
                }

            builder.create().show()
        }

        when (listType) {
            1 -> {
                val areAllGridsSet = currentItem.gridFromPlayer1 != null && currentItem.gridFromPlayer2 != null
                val ownUserTurnNumber = if (currentItem.playerName1 == AuthenticationService.getUsername()) 1 else 2
                // Check if user has something to do
                if (!GameService.isOwnGridSet(currentItem) || (areAllGridsSet && currentItem.usersTurn == ownUserTurnNumber)) {
                    holder.imageView.setImageResource(R.drawable.list_game_single_sword)
                } else {
                    holder.imageView.setImageResource(R.drawable.list_game_running)
                }
            }
            2 -> {
                holder.imageView.setImageResource(R.drawable.list_game_waiting)
            }
            else -> {
                holder.imageView.setImageResource(R.drawable.list_game_searching)
            }
        }

        currentItem.playerName2?.let {
            holder.textViewPlayer1.text = currentItem.playerName1
            holder.textViewVs.text = "vs."
            holder.textViewPlayer2.text = currentItem.playerName2
        } ?: run {
            holder.textViewPlayer1.text = "Spieler: ${currentItem.playerName1}"
            holder.textViewVs.text = ""
            holder.textViewPlayer2.text = ""
        }

        holder.textViewDate.text = DateTimeHelper.getFormattedDateAsString(currentItem.createdAt)
        holder.textViewMode.text = "(${GameService.getFleetBattleGameById(currentItem.gameDetailsId).name})"

        // Click on element in list
        holder.view.setOnClickListener { _ ->
            onClick(currentItem)
        }
    }

    override fun getItemCount() = gameList.size

    class GamesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.imageViewCreate
        val textViewPlayer1: TextView = itemView.textViewPlayer1
        val textViewVs: TextView = itemView.textViewVs
        val textViewPlayer2: TextView = itemView.textViewPlayer2
        val textViewDate: TextView = itemView.textViewDate
        val textViewMode: TextView = itemView.textViewMode
        var view = itemView
        var deleteButton: ImageButton = itemView.GameDeleteImageButton
    }
}
