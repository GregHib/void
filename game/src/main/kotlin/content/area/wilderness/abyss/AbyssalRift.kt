package content.area.wilderness.abyss

import content.entity.obj.objTeleportTakeOff
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class AbyssalRift : Script {

    init {
        objTeleportTakeOff("Exit-through", "*_rift") {
            when {
                obj.stringId == "cosmic_rift" && !player.questCompleted("lost_city") -> {
                    player.message("You need to have completed the Lost City Quest to use this rift.")
                    cancel()
                    return@objTeleportTakeOff
                }
                obj.stringId == "law_rift" -> {
                    // TODO proper message
                    player.message("You cannot carry any weapons or armour through this rift.")
                    cancel()
                    return@objTeleportTakeOff
                }
                obj.stringId == "death_rift" && !player.questCompleted("mournings_end_part_2") -> {
                    player.message("A strange power blocks your exit.")
                    cancel()
                    return@objTeleportTakeOff
                }
                obj.stringId == "blood_rift" && !player.questCompleted("legacy_of_seergaze") -> {
                    player.message("You need to have completed the Legacy of Seergaze quest to use this rift.")
                    cancel()
                    return@objTeleportTakeOff
                }
                obj.stringId == "soul_rift" -> {
                    cancel()
                    return@objTeleportTakeOff
                }
            }
        }

        objectOperate("Exit-through", "soul_rift") {
            message("You have not yet unlocked this rift.")
        }
    }
}
