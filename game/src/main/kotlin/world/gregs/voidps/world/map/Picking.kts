package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.data.definition.data.Pickable
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

val GameObject.pickable: Pickable?
    get() = def.getOrNull("pickable")

on<ObjectOption>({ option == "Pick" }) { player: Player ->
    val pickable: Pickable = obj.pickable ?: return@on
    if (player.inventory.add(pickable.item.id)) {
        player.setAnimation("climb_down")
        obj.remove(TimeUnit.SECONDS.toTicks(pickable.respawnDelay))
        player.message(pickable.message)
    } else {
        player.inventoryFull()
    }
}