package content.activity.event.random

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

/**
 * Shared behaviour for in-place "nagging" random events (Certer, Sandwich Lady, Evil Twin...).
 * The event NPC spawns next to the player, follows them, repeats nag lines, and applies the
 * ignore penalty [RandomEvents.noteAndTeleport] once its lifetime runs out. Each event's own
 * `npcOperate("Talk-to")` handler drives the puzzle and calls [endInPlaceEvent] when solved.
 */
class InPlaceRandomEvent : Script {

    init {
        npcTimerStart(NAG_TIMER) { 1 }
        npcTimerTick(NAG_TIMER) { tick(this) }
    }

    private fun tick(npc: NPC): Int {
        val owner = npc.owner
        // Owner logged out or moved on to a different event: remove this orphaned NPC quietly.
        // (The event can't be escaped by running - Follow keeps the NPC on the player - so the
        // only exits are solving it or the ignore penalty below.)
        if (owner == null || owner.get<String>("random_event") != npc.get<String>("random_event")) {
            NPCs.remove(npc)
            return Timer.CANCEL
        }
        if (!npc.watching(owner)) {
            npc.watch(owner)
        }
        val remaining = npc.dec("random_event_life", refresh = false)
        if (remaining <= 0) {
            RandomEvents.noteAndTeleport(owner)
            NPCs.remove(npc)
            return Timer.CANCEL
        }
        val interval = npc.get("random_event_nag_interval", DEFAULT_INTERVAL)
        if (remaining % interval == 0) {
            npc.nag()
        }
        return Timer.CONTINUE
    }

    private fun NPC.nag() {
        val lines: List<String> = get("random_event_nag_lines") ?: return
        if (lines.isNotEmpty()) {
            say(lines.random(random))
        }
    }

    companion object {
        const val NAG_TIMER = "random_event_nag"
        private const val DEFAULT_INTERVAL = 25
    }
}

/**
 * Spawn an in-place random event NPC beside the player. It follows the player, nags every
 * [nagInterval] ticks with a random line from [nagLines], and exiles the player via the ignore
 * penalty after [lifetime] ticks. Returns the spawned NPC.
 */
fun Player.startInPlaceEvent(
    id: String,
    nagLines: List<String>,
    lifetime: Int = 300,
    nagInterval: Int = 25,
): NPC {
    val npc = NPCs.addRandom(id, tile.toCuboid(1), owner = this) ?: NPCs.add(id, tile, ticks = -1, owner = this)
    npc["random_event"] = get<String>("random_event") ?: id
    npc["random_event_life"] = lifetime
    npc["random_event_nag_interval"] = nagInterval
    npc["random_event_nag_lines"] = nagLines
    npc.mode = Follow(npc, this)
    npc.watch(this)
    npc.softTimers.start(InPlaceRandomEvent.NAG_TIMER)
    return npc
}

/**
 * Stop an in-place event NPC's nag timer and remove it. The event's Talk-to handler calls this
 * once the puzzle is solved (or dismissed); reward + [RandomEvents.complete] remain its job.
 */
fun endInPlaceEvent(npc: NPC) {
    npc.softTimers.stop(InPlaceRandomEvent.NAG_TIMER)
    NPCs.remove(npc)
}
