package content.entity.npc

import content.entity.effect.clearTransform
import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.softQueue

class Sheep : Script {

    val items: FloorItems by inject()

    init {
        npcOperate("Shear", "sheep*") { (target) ->
            arriveDelay()
            if (!holdsItem("shears")) {
                message("You need a set of shears to do this.")
                return@npcOperate
            }
            if (target.transform.endsWith("_shorn")) {
                return@npcOperate
            }
            anim("shear_sheep")
            if (target.id == "sheep_penguin") {
                message("The... whatever it is... manages to get away from you!")
                target.mode = Retreat(target, this)
                return@npcOperate
            }
            message("You get some wool.")
            if (!inventory.add("wool")) {
                items.add(tile, "wool", revealTicks = 100, disappearTicks = 200, owner = this)
            }
            target.face(this)
            target.say("Baa!")
            target.transform("${target.id}_shorn")
            target.softQueue("regrow_wool", Settings["world.npcs.sheep.regrowTicks", 50]) {
                target.clearTransform()
            }
        }
    }
}
