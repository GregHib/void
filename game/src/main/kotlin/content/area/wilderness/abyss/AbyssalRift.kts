package content.area.wilderness.abyss

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.world.activity.quest.questComplete
import world.gregs.voidps.world.interact.entity.obj.teleportTakeOff

teleportTakeOff("Exit-through", "*_rift") {
    when {
        obj.stringId == "cosmic_rift" && !player.questComplete("lost_city") -> {
            player.message("You need to have completed the Lost City Quest to use this rift.")
            cancel()
            return@teleportTakeOff
        }
        obj.stringId == "law_rift" -> {
            // TODO proper message
            player.message("You cannot carry any weapons or armour through this rift.")
            cancel()
            return@teleportTakeOff
        }
        obj.stringId == "death_rift" && !player.questComplete("mournings_end_part_2") -> {
            player.message("A strange power blocks your exit.")
            cancel()
            return@teleportTakeOff
        }
        obj.stringId == "blood_rift" && !player.questComplete("legacy_of_seergaze") -> {
            player.message("You need to have completed the Legacy of Seergaze quest to use this rift.")
            cancel()
            return@teleportTakeOff
        }
        obj.stringId == "soul_rift" -> {
            cancel()
            return@teleportTakeOff
        }
    }
}

objectOperate("Exit-through", "soul_rift") {
    player.message("You have not yet unlocked this rift.")
    cancel()
}