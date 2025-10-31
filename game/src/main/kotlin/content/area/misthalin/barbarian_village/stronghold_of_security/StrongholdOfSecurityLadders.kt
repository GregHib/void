package content.area.misthalin.barbarian_village.stronghold_of_security

import content.entity.obj.ObjectTeleports
import content.entity.obj.objTeleportLand
import content.entity.obj.objTeleportTakeOff
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.dialogue.type.warning
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.equals

class StrongholdOfSecurityLadders : Script {

    val teleports: ObjectTeleports by inject()

    init {
        objTeleportLand("Climb-down", "stronghold_of_security_entrance") {
            player.queue("stronghold_of_security_entrance") {
                statement("You squeeze through the hole and find a ladder a few feet down leading into the Stronghold of Security.")
            }
        }

        objTeleportTakeOff("Climb-up", "stronghold_war_ladder_up") {
            if (target.tile.equals(1859, 5244)) {
                player.message("You climb up the ladder to the surface above.")
            } else {
                player.message("You climb up the ladder which seems to twist and wind in all directions.")
            }
        }

        objTeleportTakeOff("Climb-up", "stronghold_war_chain_up") {
            player.message("You climb up the chain very very carefully, squeeze through a passage then climb a ladder.")
            player.message("You climb up the ladder which seems to twist and wind in all directions.")
        }

        objTeleportTakeOff("Climb-down", "stronghold_war_ladder_down", "stronghold_famine_ladder_down") {
            if (player["warning_stronghold_of_security_ladders", 0] == 7) {
                return@objTeleportTakeOff
            }
            player.queue("stronghold_warning") {
                if (!warning("stronghold_of_security_ladders")) {
                    player<Shifty>("No thanks, I don't want to die!")
                } else {
                    player.message("You climb down the ladder to the next level.")
                    player.clear("stronghold_safe_space")
                    val definition = teleports.get(option)[target.tile.id]!!
                    teleports.teleportContinue(player, definition, this@objTeleportTakeOff)
                }
            }
            cancel()
        }

        objTeleportTakeOff("Climb-up", "stronghold_famine_rope_up", "stronghold_pestilence_vine_up", "stronghold_death_rope_up") {
            player.message("You shin up the rope, squeeze through a passage then climb a ladder.")
            player.message("You climb up the ladder which seems to twist and wind in all directions.")
        }

        objTeleportTakeOff("Climb-up", "stronghold_famine_ladder_up", "stronghold_pestilence_ladder_up", "stronghold_death_ladder_up") {
            player.message("You climb up the ladder to the level above.")
        }
    }
}
