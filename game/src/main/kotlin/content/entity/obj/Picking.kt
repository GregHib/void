package content.entity.obj

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.data.Pickable
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class Picking : Script {

    val GameObject.pickable: Pickable?
        get() = def.getOrNull("pickable")

    init {
        objectOperate("Pick") { (target) ->
            val pickable: Pickable = target.pickable ?: return@objectOperate
            if (inventory.add(pickable.item)) {
                sound("pick")
                anim("human_pickupfloor")
                if (random.nextInt(pickable.chance) == 0) {
                    target.remove(TimeUnit.SECONDS.toTicks(pickable.respawnDelay))
                }
                message(pickable.message)
            } else {
                inventoryFull()
            }
        }
    }
}
