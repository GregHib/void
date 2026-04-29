package content.area.misthalin.barbarian_village

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class BarbarianVillageChest : Script {

    init {
        objectOperate("Open", "closed_chest_barbarian_village") { (target) ->
            anim("open_chest")
            areaSound("chest_open", target.tile)
            delay(1)
            target.replace(target.id.replace("closed", "opened"), ticks = TimeUnit.MINUTES.toTicks(3))
        }

        objectOperate("Shut", "opened_chest_barbarian_village") { (target) ->
            anim("close_chest")
            areaSound("chest_close", target.tile)
            delay(1)
            target.replace(target.id.replace("opened", "closed"), ticks = TimeUnit.MINUTES.toTicks(3))
        }

        objectOperate("Search", "opened_chest_barbarian_village") {
            message("You search the chest but find nothing.")
        }
    }
}
