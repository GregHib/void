package content.skill.cooking

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class Empty : Script {

    init {
        itemOption("Empty") { (item, slot) ->
            val replacement: String = item.def.getOrNull("empty") ?: return@itemOption
            inventory.replace(slot, item.id, replacement)
            message("You empty the ${item.def.name.substringBefore(" (").lowercase()}.", ChatType.Filter)
        }

        itemOption("Empty Dish") { (item, slot) ->
            inventory.replace(slot, item.id, "pie_dish")
            message("You remove the burnt pie from the pie dish.", ChatType.Filter)
        }
    }
}
