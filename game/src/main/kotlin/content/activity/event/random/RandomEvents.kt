package content.activity.event.random

import content.quest.clearInstance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

/**
 * Registry and lifecycle for anti-macro random events.
 * https://runescape.wiki/w/Random_events?oldid=3667851
 *
 * Each event registers a launcher in its script's init block and calls
 * [complete] when the player succeeds or [fail] when they fail or abandon it.
 */
object RandomEvents {

    private val events = mutableMapOf<String, suspend Player.() -> Unit>()

    fun register(id: String, launcher: suspend Player.() -> Unit) {
        Script.checkLoading()
        events[id] = launcher
    }

    /**
     * Weighted pick over the `random_events` table, skipping rows without a registered launcher.
     */
    fun pick(): String? {
        val rows = Tables.get("random_events").rows().filter { events.containsKey(it.string("event")) }
        if (rows.isEmpty()) {
            return null
        }
        var roll = random.nextInt(rows.sumOf { it.int("weight") })
        for (row in rows) {
            roll -= row.int("weight")
            if (roll < 0) {
                return row.string("event")
            }
        }
        return null
    }

    fun start(player: Player, id: String? = pick()): Boolean {
        val launcher = events[id ?: return false] ?: return false
        player["random_event"] = id
        if (!player.contains("random_event_origin")) {
            player["random_event_origin"] = player.tile.id
        }
        player.strongQueue("random_event_start") {
            launcher.invoke(player)
        }
        return true
    }

    /**
     * Success; return the player to where they were taken from.
     */
    fun complete(player: Player) {
        player.tele(exit(player))
    }

    /**
     * Failure or abandonment; teleport the player to a random safe location with no reward.
     */
    fun fail(player: Player) {
        exit(player)
        val row = Tables.get("random_event_exiles").rows().random(random)
        player.tele(Areas[row.string("area")].random(player) ?: Tile(3222, 3218))
        player.message("You wake up feeling drowsy, unsure of where you are.")
    }

    /**
     * Ignore penalty for in-place events: note every noteable inventory item so the
     * player can't macro through a full inventory, then exile them as [fail].
     */
    fun noteAndTeleport(player: Player) {
        player.inventory.transaction {
            for (index in player.inventory.indices) {
                val item = player.inventory[index]
                if (item.isEmpty()) {
                    continue
                }
                val noteId = item.def.noteId
                if (noteId != -1) {
                    replace(index, item.id, ItemDefinitions.get(noteId).stringId)
                }
            }
        }
        fail(player)
    }

    private fun exit(player: Player): Tile {
        val origin = Tile(player["random_event_origin", player.tile.id])
        player.clearInstance()
        player.clear("random_event")
        player.clear("random_event_origin")
        cooldown(player)
        return origin
    }

    /**
     * Delay the next event by `random_event_settings` min to max minutes.
     */
    fun cooldown(player: Player) {
        val min = Tables.int("random_event_settings.min_cooldown_minutes.value")
        val max = Tables.int("random_event_settings.max_cooldown_minutes.value")
        val minutes = random.nextInt(min, max + 1)
        player.start("random_event_cooldown", TimeUnit.MINUTES.toSeconds(minutes.toLong()).toInt(), epochSeconds())
    }
}
