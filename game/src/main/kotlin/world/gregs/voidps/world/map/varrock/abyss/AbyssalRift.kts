package world.gregs.voidps.world.map.varrock.abyss

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.world.activity.quest.questComplete
import world.gregs.voidps.world.interact.entity.obj.teleportTakeOff

teleportTakeOff("Exit-through", "*_rift") {
    // TODO proper messages
    when {
        obj.stringId == "cosmic_rift" && !player.questComplete("lost_city") -> {
            player.message("You must complete the Lost City quest to exit through the this Rift.")
            cancel()
            return@teleportTakeOff
        }
        obj.stringId == "law_rift" -> {
            player.message("You cannot carry any weapons or armour through this rift.")
            cancel()
            return@teleportTakeOff
        }
        obj.stringId == "death_rift" && !player.questComplete("mournings_end_part_2") -> {
            player.message("You must complete Mourning's End Part II to exit through the this Rift.")
            cancel()
            return@teleportTakeOff
        }
        obj.stringId == "blood_rift" && !player.questComplete("legacy_of_seergaze") -> {
            player.message("You must complete the Legacy of Seergaze quest to exit through the this Rift.")
            cancel()
            return@teleportTakeOff
        }
    }
}