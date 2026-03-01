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
            val item = EnumDefinitions.stringOrNull("pickable_item", target.id) ?: return@objectOperate
            if (inventory.add(item)) {
                sound("pick")
                anim("climb_down")
                val chance = EnumDefinitions.int("pickable_chance", target.id)
                if (random.nextInt(chance) == 0) {
                    val respawnDelay = EnumDefinitions.int("pickable_respawn_delay", target.id)
                    target.remove(TimeUnit.SECONDS.toTicks(respawnDelay))
                }
                val message = EnumDefinitions.string("pickable_message", target.id)
                message(message)
            } else {
                inventoryFull()
            }
        }
    }
}
