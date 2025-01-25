package content.skill.cooking

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import content.entity.player.inv.inventoryOption

inventoryOption("Empty") {
    val replacement: String = item.def.getOrNull("empty") ?: return@inventoryOption
    player.inventory.replace(slot, item.id, replacement)
    player.message("You empty the ${item.def.name.substringBefore(" (").lowercase()}.", ChatType.Filter)
}

inventoryOption("Empty Dish") {
    player.inventory.replace(slot, item.id, "pie_dish")
    player.message("You remove the burnt pie from the pie dish.", ChatType.Filter)
}
