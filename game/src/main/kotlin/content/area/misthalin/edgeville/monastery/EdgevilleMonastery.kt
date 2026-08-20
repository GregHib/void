package content.area.misthalin.edgeville.monastery

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Teleport

class EdgevilleMonastery : Script {
    init {
        objTeleportTakeOff("Climb-up", "monastery_ladder_up") { _, _ ->
            if (!get("edgeville_monastery_order_member", false)) {
                npc<Neutral>("abbot_langley", "I'm sorry but only members of our order are allowed in the second level of the monastery.")
                choice {
                    option<Neutral>("Well can I join your order?") {
                        joinMonasteryOrder()
                    }
                    option<Sad>("Oh, sorry.")
                }
                if (!get("edgeville_monastery_order_member", false)) {
                    return@objTeleportTakeOff Teleport.CANCEL
                }
            }
            anim("climb_up")
            start("teleport_delay", 2)
            return@objTeleportTakeOff Teleport.CONTINUE
        }
    }
}
