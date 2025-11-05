package content.area.wilderness.abyss

import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Teleport

class AbyssalRift : Script {

    init {
        objTeleportTakeOff("Exit-through", "*_rift") { obj, _ ->
            when {
                obj.def(this).stringId == "cosmic_rift" && !questCompleted("lost_city") -> {
                    message("You need to have completed the Lost City Quest to use this rift.")
                    return@objTeleportTakeOff Teleport.CANCEL
                }
                obj.def(this).stringId == "law_rift" -> {
                    // TODO proper message
                    message("You cannot carry any weapons or armour through this rift.")
                    return@objTeleportTakeOff Teleport.CANCEL
                }
                obj.def(this).stringId == "death_rift" && !questCompleted("mournings_end_part_2") -> {
                    message("A strange power blocks your exit.")
                    return@objTeleportTakeOff Teleport.CANCEL
                }
                obj.def(this).stringId == "blood_rift" && !questCompleted("legacy_of_seergaze") -> {
                    message("You need to have completed the Legacy of Seergaze quest to use this rift.")
                    return@objTeleportTakeOff Teleport.CANCEL
                }
                obj.def(this).stringId == "soul_rift" -> {
                    return@objTeleportTakeOff Teleport.CANCEL
                }
            }
            return@objTeleportTakeOff Teleport.CONTINUE
        }

        objectOperate("Exit-through", "soul_rift") {
            message("You have not yet unlocked this rift.")
        }
    }
}
