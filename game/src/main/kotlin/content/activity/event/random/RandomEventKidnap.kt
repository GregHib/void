package content.activity.event.random

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Tile

/**
 * The Mysterious Old Man appears next to the player and whisks them away to an event.
 */
class RandomEventKidnap : Script {

    init {
        // An event interrupted by logout restarts fresh on login; the original
        // location persists so completing it still returns the player there.
        playerSpawn {
            val event: String = get("random_event") ?: return@playerSpawn
            RandomEvents.start(this, event)
        }

        for (type in TELEPORT_TYPES) {
            teleportTakeOff(type) {
                if (contains("random_event")) {
                    message("You can't leave just yet.")
                    false
                } else {
                    true
                }
            }
        }
    }

    companion object {
        private val TELEPORT_TYPES = listOf(
            "modern",
            "ancient",
            "lunar",
            "dungeoneering",
            "scroll",
            "tablet",
            "jewellery",
            "fairy",
            "fairy_ring",
            "spirit_tree",
            "kinship",
            "ectophial",
            "skull_sceptre",
            "puro_puro",
            "wilderness",
        )
    }
}

/**
 * The Mysterious Old Man appears beside the player to herald a teleport ("kidnap") event.
 * Kidnap-style launchers call this before [kidnap]; in-place events skip it and spawn their own
 * NPC via [startInPlaceEvent].
 */
suspend fun Player.mysteriousOldMan() {
    val npc = NPCs.addRandom("mysterious_old_man", tile.toCuboid(1), ticks = 25, owner = this)
        ?: NPCs.add("mysterious_old_man", tile, ticks = 25, owner = this)
    npc.watch(this)
    npc.say("Ah, $name, you'll do nicely!")
    delay(2)
}

/**
 * Teleport the player into an event area; events call this from their launcher.
 */
suspend fun Player.kidnap(destination: Tile) {
    areaSound("teleport", tile = tile, radius = 10)
    anim("teleport_other_impact")
    gfx("teleport_other_impact")
    delay(3)
    clearAnim()
    tele(destination)
}

/**
 * Arm the walk trigger with the event's exit [block]. Clicking off an outro starts a new
 * interaction, which kills the suspended handler; the trigger then re-queues the exit so
 * the player is still sent home with their reward. Firing disarms the trigger, and the
 * "random_event" check stops a stale queue entry from running after a normal exit.
 */
fun Player.onExitInterrupt(block: suspend Player.() -> Unit) {
    val event: String = get("random_event") ?: return
    walkTrigger = {
        queue("random_event_exit") {
            if (get<String>("random_event") == event) {
                block()
            }
        }
    }
}

/**
 * The teleport-home outro shared by kidnap events; [RandomEvents.complete] grants
 * [rewards] once the player is back. The trigger is re-armed with just the tail so an
 * interrupt during take-off can't repeat the caller's one-time effects, then cleared
 * once the trip home is inevitable.
 */
suspend fun Player.returnHome(vararg rewards: String) {
    anim("teleport_modern")
    sound("teleport")
    gfx("teleport_modern")
    onExitInterrupt { finishReturn(*rewards) }
    delay(3)
    finishReturn(*rewards)
}

private suspend fun Player.finishReturn(vararg rewards: String) {
    walkTrigger = null
    RandomEvents.complete(this, *rewards)
    anim("teleport_land_modern")
    gfx("teleport_land_modern")
    sound("teleport_land")
}
