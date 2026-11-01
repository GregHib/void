package content.quest.free.ernest_the_chicken

import content.entity.obj.door.enterDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class RubberTubeRoom : Script {
    init {
        objectOperate("Open", "door_11_closed") { (target) -> openTubeDoor(target) }

        itemOnObjectOperate("grimy_key", "door_11_closed") { (target) -> openTubeDoor(target) }

        takeable("rubber_tube") { item, telegrab -> takeTube(item, telegrab) }
    }

    private suspend fun Player.openTubeDoor(target: GameObject) {
        if (target.tile != Tile(3107, 3368)) {
            return
        }
        if (!get("ernest_tube_unlocked", false)) {
            if (!inventory.contains("grimy_key")) {
                message("The door is locked.")
                return
            }
            set("ernest_tube_unlocked", true)
            message("You unlock the door with the grimy key.")
        }
        enterDoor(target)
    }

    private fun Player.takeTube(item: FloorItem, telegrab: Boolean): String? = if (item.tile != Tile(3112, 3368)) {
        item.id
    } else if (telegrab || !get("ernest_tube_unlocked", false)) {
        message("You need to unlock the room and collect the tube from inside.")
        null
    } else {
        item.id
    }
}
