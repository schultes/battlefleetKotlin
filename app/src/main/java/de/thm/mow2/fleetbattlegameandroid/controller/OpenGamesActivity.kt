package de.thm.mow2.fleetbattlegameandroid.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.controller.adapter.GamesAdapter
import de.thm.mow2.fleetbattlegameandroid.controller.helper.GameComparator
import de.thm.mow2.fleetbattlegameandroid.controller.model.OpenGamesModel
import de.thm.mow2.fleetbattlegameandroid.controller.model.OpenGamesController
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.service.GameService
import kotlinx.android.synthetic.main.activity_open_games.*
import java.util.*
import kotlin.collections.ArrayList

class OpenGamesActivity : AppCompatActivity(), OpenGamesController {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_games)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar_open_games)

        model = OpenGamesModel(this)
        val newGameButton: Button = findViewById(R.id.newGameButton)
        val backButton: Button = findViewById(R.id.openGames_action_bar_backButton)

        newGameButton.setOnClickListener(::onClickNewGameButton)
        backButton.setOnClickListener(::onClickOpenGamesBackButton)

        spinner = findViewById(R.id.progressBar)
        spinner.visibility = View.VISIBLE

        checkIfListIsEmpty(listOfRunningGames, 1)
        checkIfListIsEmpty(listOfOwnOpenGames, 2)
        checkIfListIsEmpty(listOfOtherOpenGames, 3)

        // Load games (clear cache)
        FirebaseFirestore.getInstance().clearPersistence()
        model.loadRunningGamesOfUser()
        model.loadOpenGameRequestsOfUser()
        model.loadOtherOpenGameRequests()

        // Already running games
        runningGamesAdapter = GamesAdapter(1, true) { game ->
            redirectToOtherActivity(game)
        }
        startedGameRecyclerView.adapter = runningGamesAdapter
        startedGameRecyclerView.layoutManager = LinearLayoutManager(this)
        startedGameRecyclerView.setHasFixedSize(true)

        // Own open games
        ownOpenGamesAdapter = GamesAdapter(2, true) { game ->
            redirectToOtherActivity(game)
        }
        requestedGameRecyclerView.adapter = ownOpenGamesAdapter
        requestedGameRecyclerView.layoutManager = LinearLayoutManager(this)
        requestedGameRecyclerView.setHasFixedSize(true)

        // Other open games
        otherOpenGamesAdapter = GamesAdapter(3, false) { game ->
            model.updatePlayer2(game)
        }
        openGameRecyclerView.adapter = otherOpenGamesAdapter
        openGameRecyclerView.layoutManager = LinearLayoutManager(this)
        openGameRecyclerView.setHasFixedSize(true)
    }

    private lateinit var model: OpenGamesModel

    private var listOfRunningGames: List<FleetBattleGame> = ArrayList()
    private var listOfOwnOpenGames: List<FleetBattleGame> = ArrayList()
    private var listOfOtherOpenGames: List<FleetBattleGame> = ArrayList()

    private lateinit var runningGamesAdapter: GamesAdapter
    private lateinit var ownOpenGamesAdapter: GamesAdapter
    private lateinit var otherOpenGamesAdapter: GamesAdapter

    private lateinit var spinner: ProgressBar
    private var finishedLoadingList1 = false
    private var finishedLoadingList2 = false
    private var finishedLoadingList3 = false

    private fun redirectToOtherActivity(game: FleetBattleGame) {
        val intent = if (GameService.isOwnGridSet(game)) {
            Intent(this, GameActivity::class.java)
        } else {
            Intent(this, PrepareGameActivity::class.java)

        }
        intent.putExtra("game", game)
        startActivity(intent)
        finish()
    }

    // Show headline of list if it is not empty
    private fun checkIfListIsEmpty(list: List<FleetBattleGame>, listType: Int) {
        val showList = if (list.isEmpty()) View.GONE else View.VISIBLE
        if (list.isNotEmpty()) checkIfAllListsAreEmpty()
        when (listType) {
            1 -> textViewStartedGames.visibility = showList
            2 -> textViewRequestedGames.visibility = showList
            3 -> textViewOpenGames.visibility = showList
        }
    }

    private fun checkIfAllListsAreEmpty() {
        if (finishedLoadingList1 && finishedLoadingList2 && finishedLoadingList3) {
            if (listOfRunningGames.isEmpty() && listOfOwnOpenGames.isEmpty() && listOfOtherOpenGames.isEmpty()) {
                textViewStartedGames.text = getString(R.string.TextViewOpenGamesEmptyLists)
                textViewStartedGames.visibility = View.VISIBLE
                return
            }
        }
        textViewStartedGames.text = getString(R.string.TextViewYourOpenGames)
    }

    private fun onClickNewGameButton(view: View) {
        val intent = Intent(this, MultiplayerGameSettingsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onClickOpenGamesBackButton(view: View) {
        finish()
    }

    override fun setRunningGamesOfUser(list: List<FleetBattleGame>) {
        Collections.sort(list, GameComparator())
        listOfRunningGames = list
        finishedLoadingList1 = true
        startedGameRecyclerView.post {
            runningGamesAdapter.gameList.clear()
            runningGamesAdapter.gameList.addAll(listOfRunningGames)
            runningGamesAdapter.notifyDataSetChanged()
        }
        spinner.visibility = View.GONE
        checkIfListIsEmpty(listOfRunningGames, 1)
    }

    override fun setOpenGameRequestsOfUser(list: List<FleetBattleGame>) {
        Collections.sort(list, GameComparator())
        listOfOwnOpenGames = list
        finishedLoadingList2 = true
        requestedGameRecyclerView.post {
            ownOpenGamesAdapter.gameList.clear()
            ownOpenGamesAdapter.gameList.addAll(listOfOwnOpenGames)
            ownOpenGamesAdapter.notifyDataSetChanged()
        }
        spinner.visibility = View.GONE
        checkIfListIsEmpty(listOfOwnOpenGames, 2)
    }

    override fun setOtherOpenGameRequestsOfUser(list: List<FleetBattleGame>) {
        Collections.sort(list, GameComparator())
        listOfOtherOpenGames = list
        finishedLoadingList3 = true
        openGameRecyclerView.post {
            otherOpenGamesAdapter.gameList.clear()
            otherOpenGamesAdapter.gameList.addAll(listOfOtherOpenGames)
            otherOpenGamesAdapter.notifyDataSetChanged()
        }
        spinner.visibility = View.GONE
        checkIfListIsEmpty(listOfOtherOpenGames, 3)
    }

    override fun updateSucceed(game: FleetBattleGame) {
        redirectToOtherActivity(game)
    }

    override fun startGameResultActivity(game: FleetBattleGame) {
        val intent = Intent(this, GameResultActivity::class.java)
        intent.putExtra("game", game)
        startActivity(intent)
        finish()
    }
}