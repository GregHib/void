package content.area.misthalin.lumbridge.chicken_farm

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.floor.floorItemOperate
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import content.entity.player.bank.bank
import content.quest.questComplete
import java.util.concurrent.TimeUnit

objectOperate("Take-hatchet", "hatchet_logs") {
    if (player.inventory.add("bronze_hatchet")) {
        target.replace("logs", ticks = TimeUnit.MINUTES.toTicks(3))
    } else {
        player.inventoryFull()
    }
}

floorItemOperate("Take", "super_large_egg", override = false) {
    if (player.questComplete("cooks_assistant")) {
        player.message("You've no reason to pick that up; eggs of that size are only useful for royal cakes.")
        cancel()
    } else if (player.holdsItem("super_large_egg") || player.bank.contains("super_large_egg")) {
        player.message("You've already got one of those eggs and one's enough.")
        cancel()
    }
}