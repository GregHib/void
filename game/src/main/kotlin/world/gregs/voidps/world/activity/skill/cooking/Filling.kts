package world.gregs.voidps.world.activity.skill.cooking

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.engine.suspend.pause

itemOnObjectOperate(objects = setOf("sink*", "fountain*", "well*", "water_trough*", "pump_and_drain*"), def = "full") {
    arriveDelay()
    while (player.inventory.contains(item.id)) {
        player.setAnimation("take")
        player.inventory.replace(item.id, item.def["full"])
        pause(if (item.id == "vase") 3 else 1)
        player.message("You fill the ${item.def.name.substringBefore(" (").lowercase()} from the ${target.def.name.lowercase()}", ChatType.Filter)
    }
}