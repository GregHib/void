package content.minigame.duel_arena

import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

/**
 * Last fifty duels on this world, newest first. In-memory only.
 */
object DuelScoreboard {

    const val SIZE = 50
    private const val EMPTY = "No duels have been fought on this world yet."

    private val entries = ArrayDeque<String>(SIZE)

    val results: List<String>
        get() = entries.toList()

    fun add(winner: Player, loser: Player) {
        entries.addFirst("${winner.name} defeated ${loser.name}")
        while (entries.size > SIZE) {
            entries.removeLast()
        }
    }

    fun clear() {
        entries.clear()
    }

    /**
     * The interface builds its rows from varc strings 224-273 when it opens,
     * showing "Loading..." if every one of them is blank
     */
    fun open(player: Player) {
        for (index in 0 until SIZE) {
            player["duel_scoreboard_$index"] = entries.getOrNull(index) ?: if (index == 0) EMPTY else ""
        }
        player.open("duel_scoreboard")
    }
}
