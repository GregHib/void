package content.entity.obj

import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class BushPicking : Script {

    init {
        objectOperate("Pick-from", "cadava_bush_full,cadava_bush_half") { (target) ->
            if (!inventory.add("cadava_berries")) {
                message("Your inventory is too full to pick the berries from the bush.")
                return@objectOperate
            }
            sound("pick")
            anim("pick_plant")
            target.replace(if (target.id == "cadava_bush_full") "cadava_bush_half" else "cadava_bush_empty", ticks = Settings["world.objs.cadava.regrowTicks", 200])
        }

        objectOperate("Pick-from", "redberry_bush_full,redberry_bush_half") { (target) ->
            if (!inventory.add("redberries")) {
                message("Your inventory is too full to pick the berries from the bush.")
                return@objectOperate
            }
            sound("pick")
            anim("pick_plant")
            target.replace(if (target.id == "redberry_bush_full") "redberry_bush_half" else "redberry_bush_empty", ticks = Settings["world.objs.redberry.regrowTicks", 200])
        }

        objectOperate("Pick-from", "cadava_bush_empty,redberry_bush_empty") {
            message("There are no berries on this bush at the moment.")
        }
    }
}
