package content.area.misthalin.lumbridge.castle

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.type.warning
import content.skill.firemaking.Light
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.type.Direction

class LumbridgeCellar : Script {
    init {
        objectOperate("Bank", "culinaromancers_chest") {
            open("bank")
        }

        objectOperate("Buy-food", "culinaromancers_chest") {
            openShop("culinaromancers_chest_food_9")
        }

        objectOperate("Buy-items", "culinaromancers_chest") {
            openShop("culinaromancers_chest_9")
        }

        objectOperate("Squeeze-through", "lost_tribe_cellar_hole") { (target) ->
            val light = Light.hasLightSource(this)
            if (!light && !warning("lumbridge_cellar")) {
                message("You should find a light source and a tinderbox before going down there.")
                return@objectOperate
            }
            walkToDelay(target.tile)
            val direction = if (target.tile.x == 3219) Direction.EAST else Direction.WEST
            face(direction)
            anim("climb_through_pipe")
            exactMove(target.tile.add(direction).add(direction))
        }
    }
}
