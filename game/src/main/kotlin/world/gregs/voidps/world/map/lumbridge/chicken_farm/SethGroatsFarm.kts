package world.gregs.voidps.world.map.lumbridge.chicken_farm

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.floor.floorItemOperate
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.quest.quest
import java.util.concurrent.TimeUnit

objectOperate("Take-hatchet", "hatchet_logs") {
    if (player.inventory.add("bronze_hatchet")) {
        target.replace("logs", ticks = TimeUnit.MINUTES.toTicks(3))
    } else {
        player.inventoryFull()
    }
}

floorItemOperate("Take", "super_large_egg", Priority.HIGH) {
    if (player.quest("cooks_assistant") == "completed") {
        player.message("You've no reason to pick that up; eggs of that size are only useful for royal cakes.")
        cancel()
    }
    if (player.holdsItem("super_large_egg") || player.bank.contains("super_large_egg")) {
        player.message("You've already got one of those eggs and one's enough.")
        cancel()
    }
}