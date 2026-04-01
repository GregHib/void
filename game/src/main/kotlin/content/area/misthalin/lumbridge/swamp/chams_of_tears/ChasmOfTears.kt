package content.area.misthalin.lumbridge.swamp.chams_of_tears

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Direction

class ChasmOfTears : Script {
    init {
        objectOperate("Climb", "chasm_of_tears_rocks") { (target) ->
            val direction = if (tile.x < target.tile.x) Direction.EAST else Direction.WEST
            walkToDelay(target.tile.add(direction.inverse()))
            anim("chasm_of_tears_climb_slope_${if (direction == Direction.EAST) "down" else "up"}")
            face(direction)
            sound("climbing_loop", repeat = 5)
            exactMoveDelay(target.tile.add(direction), delay = 100, direction = Direction.WEST)
        }

        objectOperate("Climb", "chasm_of_tears_rocks_down") { (target) ->
            if (tile.x >= target.tile.x) {
                message("You could climb down here, but it is too uneven to climb up.")
                return@objectOperate
            }
            val direction = Direction.EAST
            walkToDelay(target.tile.add(direction.inverse()))
            anim("chasm_of_tears_climb_slope_down")
            face(direction)
            sound("climbing_loop", repeat = 5)
            exactMoveDelay(target.tile.add(direction), delay = 100, direction = Direction.WEST)
        }
    }
}
