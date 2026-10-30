package content.area.misthalin.varrock

import content.entity.obj.door.closeDoor
import content.entity.obj.door.enterDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class VarrockWestGate : Script {

    private val palaceGateClosedTile = Tile(3245, 3501)
    private val palaceGateOpenedTile = Tile(3245, 3502)

    init {
        objectOperate("Open", "gate_west_varrock_closed,gate_west_varrock_2_closed") { (target) ->
            if (target.tile == palaceGateClosedTile) {
                val fromNorth = tile.y > target.tile.y
                val destination = if (fromNorth) palaceGateClosedTile else palaceGateOpenedTile
                val direction = if (fromNorth) Direction.SOUTH else Direction.NORTH
                val exit = destination.addY(if (fromNorth) -1 else 1)
                if (!fromNorth) {
                    walkOverDelay(target.tile)
                }
                target.replace("gate_west_varrock_opened", tile = palaceGateOpenedTile, rotation = 0, ticks = 3, collision = false) {
                    areaSound("gate_close", palaceGateClosedTile)
                }
                sound("gate_open")
                delay()
                walkOverDelay(destination)
                walkOverDelay(exit)
                clear("face_entity")
                face(direction)
                return@objectOperate
            }
            enterDoor(target)
        }

        objectOperate("Close", "gate_west_varrock_opened,gate_west_varrock_2_opened") { (target) ->
            if (target.tile == palaceGateOpenedTile && target.rotation == 0) {
                target.replace("gate_west_varrock_closed", tile = palaceGateClosedTile, rotation = 1)
                sound("gate_close")
                return@objectOperate
            }
            closeDoor(target)
        }
    }
}
