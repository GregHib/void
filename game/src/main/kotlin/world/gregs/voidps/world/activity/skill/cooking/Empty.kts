package world.gregs.voidps.world.activity.skill.cooking

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOption

inventoryOption("Empty") {
    val replacement = when {
        item.id.startsWith("bucket_of") || item.id.endsWith("compost") || item.id.endsWith("dung") -> "bucket"
        item.id.startsWith("jug_of") -> "jug"
        item.id.startsWith("pot_of") -> "empty_pot"
        item.id.startsWith("bowl_of") -> "bowl"
        else -> "vial"
    }
    player.inventory.replace(slot, item.id, replacement)
    player.message("You empty the ${item.def.name.substringBefore(" (").lowercase()}.", ChatType.Filter)
}

inventoryOption("Empty Dish") {
    player.inventory.replace(slot, item.id, "pie_dish")
    player.message("You remove the burnt pie from the pie dish.", ChatType.Filter)
}
