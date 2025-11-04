package content.area.misthalin.lumbridge.farm

import content.entity.player.bank.ownsItem
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class SethGroatsFarm : Script {

    init {
        objectOperate("Take-hatchet", "hatchet_logs") { (target) ->
            if (inventory.add("bronze_hatchet")) {
                target.replace("logs", ticks = TimeUnit.MINUTES.toTicks(3))
            } else {
                inventoryFull()
            }
        }

        takeable("super_large_egg") { item ->
            if (questCompleted("cooks_assistant")) {
                message("You've no reason to pick that up; eggs of that size are only useful for royal cakes.")
                null
            } else if (ownsItem("super_large_egg")) {
                message("You've already got one of those eggs and one's enough.")
                null
            } else {
                item
            }
        }
    }
}
