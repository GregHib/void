package content.entity.obj

import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

@Script
class BushPicking {

    init {
        objectOperate("Pick-from", "cadava_bush_full", "cadava_bush_half") {
            if (!player.inventory.add("cadava_berries")) {
                player.message("Your inventory is too full to pick the berries from the bush.")
                return@objectOperate
            }
            player.sound("pick")
            player.anim("pick_plant")
            target.replace(if (target.id == "cadava_bush_full") "cadava_bush_half" else "cadava_bush_empty", ticks = Settings["world.objs.cadava.regrowTicks", 200])
        }

        objectOperate("Pick-from", "redberry_bush_full", "redberry_bush_half") {
            if (!player.inventory.add("redberries")) {
                player.message("Your inventory is too full to pick the berries from the bush.")
                return@objectOperate
            }
            player.sound("pick")
            player.anim("pick_plant")
            target.replace(if (target.id == "redberry_bush_full") "redberry_bush_half" else "redberry_bush_empty", ticks = Settings["world.objs.redberry.regrowTicks", 200])
        }

        objectOperate("Pick-from", "cadava_bush_empty", "redberry_bush_empty") {
            player.message("There are no berries on this bush at the moment.")
        }
    }
}
