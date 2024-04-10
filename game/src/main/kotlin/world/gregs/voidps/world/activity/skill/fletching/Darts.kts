package world.gregs.voidps.world.activity.skill.fletching

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.data.definition.data.FletchDarts
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory

itemOnItem("feather", "*_dart_tip") {
    val dartUsed = if (toItem.dartUnf) toItem else fromItem
    val darts: FletchDarts = dartUsed.def.getOrNull("fletch_dart") ?: return@itemOnItem

    if (!it.has(Skill.Fletching, darts.level, true)) {
        return@itemOnItem
    }

    val currentFeathers = it.inventory.count("feather")
    val currentDartTips = it.inventory.count(dartUsed.id)

    // Calculate the actual number of darts that can be made
    val actualAmount = minOf(currentFeathers, currentDartTips, 10)

    // If we can't make any darts, exit
    if (actualAmount < 1) {
        return@itemOnItem
    }

    val createdDart: String = dartUsed.id.replace("_tip", "")
    it.inventory.transaction {
        it.setAnimation("fletch_darts")
        remove(dartUsed.id, actualAmount)
        remove("feather", actualAmount)
        add(createdDart, actualAmount)

        // Calculate and award experience based on the actual amount made
        val totalExperience = darts.xp * actualAmount
        it.experience.add(Skill.Fletching, totalExperience)

        it.message("You finish making $actualAmount darts.", ChatType.Game)
    }
}


val Item.dartUnf: Boolean
    get() = def.contains("fletch_dart")