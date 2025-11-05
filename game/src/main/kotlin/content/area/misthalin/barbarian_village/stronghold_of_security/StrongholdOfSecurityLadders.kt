package content.area.misthalin.barbarian_village.stronghold_of_security

import content.entity.obj.ObjectTeleports
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.dialogue.type.warning
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.equals

class StrongholdOfSecurityLadders : Script {

    val teleports: ObjectTeleports by inject()

    init {
        objTeleportLand("Climb-down", "stronghold_of_security_entrance") { _, _ ->
            queue("stronghold_of_security_entrance") {
                statement("You squeeze through the hole and find a ladder a few feet down leading into the Stronghold of Security.")
            }
        }

        objTeleportTakeOff("Climb-up", "stronghold_war_ladder_up") { target, _ ->
            if (target.tile.equals(1859, 5244)) {
                message("You climb up the ladder to the surface above.")
            } else {
                message("You climb up the ladder which seems to twist and wind in all directions.")
            }
            Teleport.CONTINUE
        }

        objTeleportTakeOff("Climb-up", "stronghold_war_chain_up") { _, _ ->
            message("You climb up the chain very very carefully, squeeze through a passage then climb a ladder.")
            message("You climb up the ladder which seems to twist and wind in all directions.")
            Teleport.CONTINUE
        }

        objTeleportTakeOff("Climb-down", "stronghold_war_ladder_down,stronghold_famine_ladder_down") { target, option ->
            if (get("warning_stronghold_of_security_ladders", 0) == 7) {
                return@objTeleportTakeOff Teleport.CONTINUE
            }
            queue("stronghold_warning") {
                if (!warning("stronghold_of_security_ladders")) {
                    player<Shifty>("No thanks, I don't want to die!")
                } else {
                    message("You climb down the ladder to the next level.")
                    clear("stronghold_safe_space")
                    val definition = teleports.get(option)[target.tile.id]!!
                    teleports.teleportContinue(player, definition, target)
                }
            }
            Teleport.CANCEL
        }

        objTeleportTakeOff("Climb-up", "stronghold_famine_rope_up,stronghold_pestilence_vine_up,stronghold_death_rope_up") { _, _ ->
            message("You shin up the rope, squeeze through a passage then climb a ladder.")
            message("You climb up the ladder which seems to twist and wind in all directions.")
            Teleport.CONTINUE
        }

        objTeleportTakeOff("Climb-up", "stronghold_famine_ladder_up,stronghold_pestilence_ladder_up,stronghold_death_ladder_up") { _, _ ->
            message("You climb up the ladder to the level above.")
            Teleport.CONTINUE
        }
    }
}
