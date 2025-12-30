package content.entity.obj.door

import world.gregs.voidps.engine.Script

class Doors : Script {

    init {
        objectOperate("Close") { (target) ->
            closeDoor(target)
        }

        objectOperate("Open") { (target) ->
            openDoor(target)
        }
    }

}
