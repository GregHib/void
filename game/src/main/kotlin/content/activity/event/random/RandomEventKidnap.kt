package content.activity.event.random

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
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
