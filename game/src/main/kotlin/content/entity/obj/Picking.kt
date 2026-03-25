package content.entity.obj

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class Picking : Script {

    init {
        objectOperate("Pick") { (target) ->
            val pickable = EnumDefinitions.rowOrNull("pickables", target.id) ?: return@objectOperate
            val item = pickable.item("item")
            if (!inventory.add(item)) {
                inventoryFull()
                return@objectOperate
            }
            sound("pick")
            anim("climb_down")
            val chance = pickable.int("chance")
            if (random.nextInt(chance) == 0) {
                val respawnDelay = pickable.int("respawn")
                target.remove(TimeUnit.SECONDS.toTicks(respawnDelay))
            }
            message(pickable.string("message"))
        }
    }
}
