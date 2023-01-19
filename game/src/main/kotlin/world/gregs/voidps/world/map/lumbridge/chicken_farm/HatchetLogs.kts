package world.gregs.voidps.world.map.lumbridge.chicken_farm

import world.gregs.voidps.engine.entity.character.contain.add
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.mode.interact.onOperate
import world.gregs.voidps.engine.entity.character.mode.interact.option.option
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.utility.toTicks
import java.util.concurrent.TimeUnit

onOperate({ target.id == "hatchet_logs" && option == "Take-hatchet" }, Priority.HIGH) { player: Player, obj: GameObject ->
    if (player.inventory.add("bronze_hatchet")) {
        obj.replace("logs", ticks = TimeUnit.MINUTES.toTicks(3))
    } else {
        player.inventoryFull()
    }
}