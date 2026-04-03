package content.area.morytania.canifis

import content.entity.obj.door.DoubleDoor
import content.entity.obj.door.enterDoor
import content.entity.obj.door.openDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.replace

class CanifisTavern : Script {
    init {
        objectOperate("Search", "canifis_fake_wall_closed") { (target) ->
            enterDoor(target)
        }

        objectOperate("Open", "canifis_wooden_door_left_closed,canifis_wooden_door_right_closed") { (target) ->
            anim("take")
            sound("locked")
            delay(1)
            target.replace(target.id.replace("_closed", "_opened"), ticks = 3)
            val other = DoubleDoor.get(target, target.def, 0)
            other?.replace(other.id.replace("_closed", "_opened"), ticks = 3)
            delay(2)
            tele(3509, 3449)
        }
    }
}