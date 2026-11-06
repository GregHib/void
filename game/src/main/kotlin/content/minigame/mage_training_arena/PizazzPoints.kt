package content.minigame.mage_training_arena

import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Pizazz points are stored in four varbits read by the rewards interface and shown on each room's overlay.
 */
object PizazzPoints {
    private val limits = mapOf(
        "telekinetic" to 8191,
        "alchemist" to 8191,
        "enchanting" to 32767,
        "graveyard" to 16383,
    )

    private fun key(room: String) = "mage_training_arena_${room}_points"

    fun get(player: Player, room: String): Int = player[key(room), 0]

    fun total(player: Player): Int = Tables.get("mta_rooms").rows().sumOf { get(player, it.rowId) }

    fun add(player: Player, room: String, amount: Int) {
        if (amount <= 0) {
            return
        }
        player.inc(key(room), amount, max = limits.getValue(room))
        refresh(player, room)
        PizazzHat.refresh(player)
    }

    fun remove(player: Player, room: String, amount: Int) {
        if (amount <= 0) {
            return
        }
        player.dec(key(room), amount, min = 0)
        refresh(player, room)
    }

    fun reset(player: Player) {
        for (room in Tables.get("mta_rooms").rows()) {
            player[key(room.rowId)] = 0
            refresh(player, room.rowId)
        }
    }

    fun refresh(player: Player, room: String) {
        val overlay = Rows.getOrNull("mta_rooms.$room")?.string("overlay") ?: return
        if (!player.hasOpen(overlay)) {
            return
        }
        player.interfaces.sendText(overlay, "points", get(player, room).toString())
    }
}
