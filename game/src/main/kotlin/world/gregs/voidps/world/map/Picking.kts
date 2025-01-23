package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.data.Pickable
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

val GameObject.pickable: Pickable?
    get() = def.getOrNull("pickable")

objectOperate("Pick") {
    val pickable: Pickable = target.pickable ?: return@objectOperate
    if (player.inventory.add(pickable.item)) {
        player.setAnimation("climb_down")
        target.remove(TimeUnit.SECONDS.toTicks(pickable.respawnDelay))
        player.message(pickable.message)
    } else {
        player.inventoryFull()
    }
}