package world.gregs.voidps.world.map.lumbridge.chicken_farm

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.activity.bank.bank
import java.util.concurrent.TimeUnit

on<ObjectOption>({ operate && obj.id == "hatchet_logs" && option == "Take-hatchet" }, Priority.HIGH) { player: Player ->
    if (player.inventory.add("bronze_hatchet")) {
        obj.replace("logs", ticks = TimeUnit.MINUTES.toTicks(3))
    } else {
        player.inventoryFull()
    }
}

on<FloorItemOption>({ operate && item.id == "super_large_egg" && option == "Take" }, Priority.HIGH){ player: Player ->
    if (player["cooks_assistant", "unstarted"] == "completed") {
        player.message("You've no reason to pick that up; eggs of that size are only useful for royal cakes.")
        cancel()
    }
    if (player.hasItem("super_large_egg") || player.bank.contains("super_large_egg")) {
        player.message("You've already got one of those eggs and one's enough.")
        cancel()
    }
}