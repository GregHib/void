package world.gregs.voidps.world.activity.skill.fletching

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.data.definition.data.FletchBolts
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory

itemOnItem("feather", "*_bolts_unf") {
    val boltUsed = if (toItem.boltsUnf) toItem else fromItem
    val bolts: FletchBolts = boltUsed.def.getOrNull("fletch_bolts") ?: return@itemOnItem

    if (!it.has(Skill.Fletching, bolts.level, true)) {
        return@itemOnItem
    }

    val currentFeathers = it.inventory.count("feather")
    val currentBoltUnfs = it.inventory.count(boltUsed.id)

    val actualAmount = minOf(currentFeathers, currentBoltUnfs, 10)

    if (actualAmount < 1) {
        return@itemOnItem
    }

    val createdBolt: String = boltUsed.id.replace("_unf", "")
    val success = it.inventory.transaction {
        remove(boltUsed.id, actualAmount)
        remove("feather", actualAmount)
        add(createdBolt, actualAmount)
    }

    if(!success) {
        return@itemOnItem
    }

    it.setAnimation("tipping_bolts")
    val totalExperience = bolts.xp * actualAmount
    it.experience.add(Skill.Fletching, totalExperience)
    it.message("You fletch $actualAmount bolts.", ChatType.Game)
}

val Item.boltsUnf: Boolean
    get() = def.contains("fletch_bolts")