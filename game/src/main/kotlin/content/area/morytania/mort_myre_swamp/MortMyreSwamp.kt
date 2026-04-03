package content.area.morytania.mort_myre_swamp

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class MortMyreSwamp : Script {
    init {
        objectOperate("Open", "swamp_wooden_doors_closed") { (target) ->
            anim("take")
            sound("locked")
            delay(1)
            target.replace("swamp_wooden_doors_opened", ticks = 3)
            delay(2)
            tele(3500, 9811)
        }

        objectOperate("Climb", "swamp_bridge_tree") { (target) ->
            val direction = if (target.tile.y == 3431) Direction.SOUTH else Direction.NORTH
            anim("climb_up")
            delay(2)
            tele(target.tile.add(direction))
            message("You climb up the tree.")
            face(direction)
            delay(1)
            walkOverDelay(target.tile.add(direction).add(direction).add(direction).add(direction))
            message("You climb down the tree.")
            anim("climb_down")
            delay(1)
            tele(if (direction == Direction.SOUTH) Tile(3502, 3425) else Tile(3503, 3431))
        }
    }
}