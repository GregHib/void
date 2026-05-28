package content.skill.dungeoneering

import content.quest.instance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction

class DungeonDoors : Script {
    init {
        objectOperate("Enter", "*door_frozen,*door_abandoned,*door_furnished,*door_occult,*door_warped") { (target) ->
            openDoor(target.rotation)
        }

        objectOperate("Unlock", "orange_*_door,silver_*_door,yellow_*_door,green_*_door,blue_*_door,purple_*_door,crimson_*_door,gold_*_door") { (target) ->
            val dungeon = dungeonMap ?: return@objectOperate
            val instance = instance() ?: return@objectOperate
            val origin = tile.delta(instance.tile)
            val roomTile = origin.room
            val room = dungeon.room(roomTile.x, roomTile.y) ?: return@objectOperate
            val door = room.doors[target.rotation] ?: return@objectOperate
            if (door is DungeonDoor.Locked) {
                if (!inventory.remove(door.key)) {
                    message("You don't have the correct key.")
                    return@objectOperate
                }
                target.remove()
            }
        }

        objectOperate("Fix-pulley", "broken_pulley_door_*") { (target) ->
            target.replace(target.id.replace("broken_pulley", "fixed_pulley"))
        }

        objectOperate("Open", "*door_frozen,*door_abandoned,*door_furnished,*door_occult,*door_warped") { (target) ->
            openDoor(target.rotation)
        }
    }

    val Delta.room: Delta
        get() = Delta(x / 16, y / 16)

    private fun Player.openDoor(rotation: Int) {
        val dungeon = dungeonMap ?: return
        val instance = instance() ?: return
        val origin = tile.delta(instance.tile)
        val roomTile = origin.room
        val direction = when (rotation) {
            0 -> Direction.WEST
            1 -> Direction.NORTH
            2 -> Direction.EAST
            3 -> Direction.SOUTH
            else -> return
        }
        val room = dungeon.room(roomTile.x, roomTile.y) ?: return
        val adj = room.adjacentRooms[rotation] ?: return
        if (adj.open) {
            tele(tile.add(direction).add(direction).add(direction))
            sendScript("dung_map_add_player", roomTile.x, roomTile.y, rotation, 1, name)
            return
        }
        adj.open(this, dungeon)
    }
}