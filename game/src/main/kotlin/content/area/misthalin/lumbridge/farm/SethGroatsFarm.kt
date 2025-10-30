package content.area.misthalin.lumbridge.farm

import content.entity.player.bank.ownsItem
import content.entity.player.inv.item.take.canTake
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class SethGroatsFarm : Script {

    init {
        objectOperate("Take-hatchet", "hatchet_logs") {
            if (player.inventory.add("bronze_hatchet")) {
                target.replace("logs", ticks = TimeUnit.MINUTES.toTicks(3))
            } else {
                player.inventoryFull()
            }
        }

        canTake("super_large_egg") { player ->
            if (player.questCompleted("cooks_assistant")) {
                player.message("You've no reason to pick that up; eggs of that size are only useful for royal cakes.")
                cancel()
            } else if (player.ownsItem("super_large_egg")) {
                player.message("You've already got one of those eggs and one's enough.")
                cancel()
            }
        }
    }
}
