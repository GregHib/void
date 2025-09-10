package content.area.misthalin.barbarian_village.stronghold_of_security

import content.entity.obj.objTeleportTakeOff
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.event.Script
@Script
class StrongholdOfSecurityPortals {

    init {
        objTeleportTakeOff("Enter", "stronghold_war_portal") {
            if (player["unlocked_emote_flap", false] || player.combatLevel > 25) {
                player.clear("stronghold_safe_space")
                player.message("You enter the portal to be whisked through to the treasure room.", ChatType.Filter)
                return@objTeleportTakeOff
            }
            player.message("You are not of sufficient experience to take the shortcut through this level.")
            cancel()
        }

        objTeleportTakeOff("Enter", "stronghold_famine_portal") {
            if (player["unlocked_emote_slap_head", false] || player.combatLevel > 50) {
                player.clear("stronghold_safe_space")
                player.message("You enter the portal to be whisked through to the treasure room.", ChatType.Filter)
                return@objTeleportTakeOff
            }
            player.message("You are not of sufficient experience to take the shortcut through this level.")
            cancel()
        }

        objTeleportTakeOff("Enter", "stronghold_pestilence_portal") {
            if (player["unlocked_emote_idea", false] || player.combatLevel > 75) {
                player.clear("stronghold_safe_space")
                player.message("You enter the portal to be whisked through to the treasure room.", ChatType.Filter)
                return@objTeleportTakeOff
            }
            player.message("You are not of sufficient experience to take the shortcut through this level.")
            cancel()
        }

        objTeleportTakeOff("Enter", "stronghold_death_portal") {
            if (player["unlocked_emote_stomp", false]) {
                player.clear("stronghold_safe_space")
                player.message("You enter the portal to be whisked through to the treasure room.", ChatType.Filter)
                return@objTeleportTakeOff
            }
            player.message("You must have completed this level to take this shortcut.")
            cancel()
        }

    }

}
