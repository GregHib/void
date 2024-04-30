package world.gregs.voidps.world.map.varrock.abyss

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.activity.quest.questComplete
import world.gregs.voidps.world.interact.entity.obj.Teleports


val teleports: Teleports by inject()

objectOperate("Exit-through", "*_rift") {
    // TODO proper messages
    when {
        target.id == "cosmic_rift" && !player.questComplete("lost_city") -> {
            player.message("You must complete the Lost City quest to exit through the this Rift.")
            return@objectOperate
        }
        target.id == "law_rift" -> {
            player.message("You cannot carry any weapons or armour through this rift.")
            return@objectOperate
        }
        target.id == "death_rift" && !player.questComplete("mournings_end_part_2") -> {
            player.message("You must complete Mourning's End Part II to exit through the this Rift.")
            return@objectOperate
        }
        target.id == "blood_rift" && !player.questComplete("legacy_of_seergaze") -> {
            player.message("You must complete the Legacy of Seergaze quest to exit through the this Rift.")
            return@objectOperate
        }
    }
    val type = target.id.removeSuffix("_rift")
    val teleport = teleports.get("${type}_altar_portal", "Enter").first()
    player.tele(teleport.to) // TODO proper coords
}