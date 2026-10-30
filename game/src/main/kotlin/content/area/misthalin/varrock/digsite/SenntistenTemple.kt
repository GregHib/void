package content.area.misthalin.varrock.digsite

import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Teleport

class SenntistenTemple : Script {

    init {
        // TODO the Dig Site quest also uses this winch to lower into the dig shafts
        objTeleportTakeOff("Operate", "digsite_winch_west") { _, _ ->
            if (!questCompleted("temple_at_senntisten")) {
                message("You must have completed The Temple at Senntisten quest.")
                return@objTeleportTakeOff Teleport.CANCEL
            }
            Teleport.CONTINUE
        }
    }
}
