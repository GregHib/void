package world.gregs.voidps.world.map.lumbridge.chicken_farm

import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.toTicks
import java.util.concurrent.TimeUnit

on<ObjectOption>({ obj.id == "hatchet_logs" && option == "Take-hatchet" }, Priority.HIGH) { player: Player ->
    if (player.inventory.add("bronze_hatchet")) {
        obj.replace("logs", ticks = TimeUnit.MINUTES.toTicks(3))
    } else {
        player.inventoryFull()
    }
}