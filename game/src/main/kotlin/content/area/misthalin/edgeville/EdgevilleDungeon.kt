package content.area.misthalin.edgeville

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.type.warning
import world.gregs.voidps.engine.Script

class EdgevilleDungeon : Script {
    init {
        objectOperate("Open", "edgeville_dungeon_wilderness_west_metal_door_closed,edgeville_dungeon_wilderness_east_metal_door_closed") { (target) ->
            if (tile.y <= 9917 && !warning("wilderness")) {
                return@objectOperate
            }
            enterDoor(target, delay = 1)
        }
    }
}
