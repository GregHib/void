package content.minigame.duel_arena

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

/**
 * Last fifty duels on this world, newest first. In-memory only.
 */
object DuelScoreboard {

    const val SIZE = 50
    private const val ROW_HEIGHT = 16

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

    fun open(player: Player) {
        if (!player.open("duel_scoreboard")) {
            return
        }
        val list = InterfaceDefinitions.getComponent("duel_scoreboard", "list") ?: return
        val scrollbar = InterfaceDefinitions.getComponent("duel_scoreboard", "scrollbar") ?: return
        for ((index, entry) in entries.withIndex()) {
            player.sendScript("duel_scoreboard_row", 0, index * ROW_HEIGHT, list.id, entry)
        }
        player.sendScript("scrollbar_vertical", scrollbar.id, list.id)
        player.sendScript("set_scroll_height", scrollbar.id, list.id, entries.size * ROW_HEIGHT, 0)
    }
}
