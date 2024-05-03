package world.gregs.voidps.world.map.varrock.abyss

import world.gregs.voidps.engine.client.message
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
            player.message("You need to have completed Troll Stronghold to use this rift.")
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
            player.message("You have not yet unlocked this rift.")
            cancel()
            return@teleportTakeOff
        }
    }
}