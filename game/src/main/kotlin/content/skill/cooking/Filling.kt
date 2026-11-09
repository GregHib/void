package content.skill.cooking

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.type.Tile

class Filling : Script {

    init {
        itemOnObjectOperate(obj = "sink*,fountain*,well*,water_trough*,pump_and_drain*,pump_and_tub*,water_barrel_*,waterpump_*") { (target, item) ->
            // The manor fountain has quest-specific piranha/poison handling instead of drinkable water.
            if (target.id == "fountain" && target.tile == Tile(3087, 3334)) return@itemOnObjectOperate
            if (!item.def.contains("full")) {
                return@itemOnObjectOperate
            }
            while (inventory.contains(item.id)) {
                anim("take")
                inventory.replace(item.id, item.def["full"])
                delay(if (item.id == "vase") 3 else 1)
                message("You fill the ${item.def.name.substringBefore(" (").lowercase()} from the ${target.def.name.lowercase()}", ChatType.Filter)
            }
        }
    }
}
