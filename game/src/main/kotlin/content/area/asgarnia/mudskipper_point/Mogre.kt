package content.area.asgarnia.mudskipper_point

import content.entity.combat.hit.directHit
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

/**
 * Mogres are lured out of the water at Mudskipper Point by throwing fishing explosives at the ominous fishing spots,
 * once Skippy has explained where they come from.
 */
class Mogre : Script {

    private val shouts = listOf(
        "Tasty human!",
        "I smack you good!",
        "Human hit me on the head!",
        "Smash stupid human!",
        "I get you!",
        "Human scare all da fishies!",
        "Da boom-boom kill all da fishies!",
    )

    init {
        objectApproach("Lure", "ominous_fishing_spot*") {
            message("Something seems to have scared all the fishes away...")
        }

        objectApproach("Bait", "ominous_fishing_spot*") {
            message("Something seems to have scared all the fishes away...")
        }

        itemOnObjectApproach("fishing_explosive,super_fishing_explosive", "ominous_fishing_spot*") { (target, item) ->
            approachRange(10)
            if (get("mogre_miniquest", 0) < 3) {
                noInterest()
                return@itemOnObjectApproach
            }
            if (tile.within(target.tile, 2)) {
                player<Neutral>("If this thing explodes, I think I should stand a liiiiitle further away...")
                return@itemOnObjectApproach
            }
            if (NPCs.any { it.id == "mogre" && it["owner", ""] == accountName }) {
                message("There is already one Mogre attacking you!")
                return@itemOnObjectApproach
            }
            if (item.id == "super_fishing_explosive" && !questCompleted("kenniths_concerns")) {
                message("You must complete Kennith's Concerns to use this explosive.")
                return@itemOnObjectApproach
            }
            if (!inventory.remove(item.id)) {
                return@itemOnObjectApproach
            }
            face(target)
            anim("throw_fishing_explosive")
            message("You hurl the shuddering vial into the water...")
            val player = this
            queue("mogre_lure", 2 + tile.distanceTo(target.tile) / 2) {
                val tile = target.tile.toCuboid(2).random(player) ?: player.tile
                val mogre = NPCs.add("mogre", tile, ticks = 1000, owner = player)
                mogre["owner_index"] = player.index
                mogre.gfx("big_splash")
                mogre.say(shouts.random(random))
                if (item.id == "super_fishing_explosive") {
                    mogre.directHit(150)
                }
                message("...and a Mogre appears!")
                mogre.interactPlayer(player, "Attack")
                mogre.softTimers.start("mogre_leash")
            }
        }

        // Only the player who lured a mogre out can fight it
        canAttack("mogre") { target ->
            val owner: String = target["owner", ""]
            owner == "" || owner == accountName
        }

        npcTimerStart("mogre_leash") { 2 }

        // Mogres return to the water when the player who lured them leaves
        npcTimerTick("mogre_leash") {
            val owner = Players.indexed(this["owner_index", -1])
            if (owner == null || !owner.tile.within(tile, 15)) {
                NPCs.remove(this)
                return@npcTimerTick Timer.CANCEL
            }
            Timer.CONTINUE
        }
    }
}
