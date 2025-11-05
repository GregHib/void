package content.area.misthalin.barbarian_village.stronghold_of_security

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.combatLevel

class StrongholdOfSecurityPortals : Script {

    init {
        objTeleportTakeOff("Enter", "stronghold_war_portal") { _, _ ->
            if (get("unlocked_emote_flap", false) || combatLevel > 25) {
                clear("stronghold_safe_space")
                message("You enter the portal to be whisked through to the treasure room.", ChatType.Filter)
                return@objTeleportTakeOff Teleport.CONTINUE
            }
            message("You are not of sufficient experience to take the shortcut through this level.")
            return@objTeleportTakeOff Teleport.CANCEL
        }

        objTeleportTakeOff("Enter", "stronghold_famine_portal") { _, _ ->
            if (get("unlocked_emote_slap_head", false) || combatLevel > 50) {
                clear("stronghold_safe_space")
                message("You enter the portal to be whisked through to the treasure room.", ChatType.Filter)
                return@objTeleportTakeOff Teleport.CONTINUE
            }
            message("You are not of sufficient experience to take the shortcut through this level.")
            return@objTeleportTakeOff Teleport.CANCEL
        }

        objTeleportTakeOff("Enter", "stronghold_pestilence_portal") { _, _ ->
            if (get("unlocked_emote_idea", false) || combatLevel > 75) {
                clear("stronghold_safe_space")
                message("You enter the portal to be whisked through to the treasure room.", ChatType.Filter)
                return@objTeleportTakeOff Teleport.CONTINUE
            }
            message("You are not of sufficient experience to take the shortcut through this level.")
            return@objTeleportTakeOff Teleport.CANCEL
        }

        objTeleportTakeOff("Enter", "stronghold_death_portal") { _, _ ->
            if (get("unlocked_emote_stomp", false)) {
                clear("stronghold_safe_space")
                message("You enter the portal to be whisked through to the treasure room.", ChatType.Filter)
                return@objTeleportTakeOff Teleport.CONTINUE
            }
            message("You must have completed this level to take this shortcut.")
            return@objTeleportTakeOff Teleport.CANCEL
        }
    }
}
