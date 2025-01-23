package world.gregs.voidps.world.activity.skill.cooking

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

itemOnObjectOperate(objects = setOf("sink*", "fountain*", "well*", "water_trough*", "pump_and_drain*"), def = "full") {
    while (player.inventory.contains(item.id)) {
        player.anim("take")
        player.inventory.replace(item.id, item.def["full"])
        delay(if (item.id == "vase") 3 else 1)
        player.message("You fill the ${item.def.name.substringBefore(" (").lowercase()} from the ${target.def.name.lowercase()}", ChatType.Filter)
    }
}