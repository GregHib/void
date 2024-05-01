package world.gregs.voidps.world.activity.skill.firemaking

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.data.definition.data.LightSources
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.world.interact.entity.player.equip.inventoryItem


itemOnItem("tinderbox*") {
    val needsFlame: LightSources = toItem.def.getOrNull("light_source") ?: return@itemOnItem

    if (!it.has(Skill.Firemaking, needsFlame.level, true)) {
        return@itemOnItem
    }

    it.inventory.transaction {
        replace(toItem.id, needsFlame.onceLit)
    }

    val litItem = determineLightSource(needsFlame.onceLit)
    it.message("You light the $litItem", ChatType.Game)

}

inventoryItem("Extinguish") {
    val toExtinguish: LightSources = item.def.getOrNull("light_source") ?: return@inventoryItem
    player.inventory.transaction {
        replace(item.id, toExtinguish.onceExtinguish)
    }

    player.message("You extinguish the flame.", ChatType.Game)
}

fun determineLightSource(itemName: String): String {
    return when {
        itemName.contains("lantern", ignoreCase = true) -> "lantern."
        itemName.contains("candle", ignoreCase = true) -> "candle."
        else -> "null"
    }
}