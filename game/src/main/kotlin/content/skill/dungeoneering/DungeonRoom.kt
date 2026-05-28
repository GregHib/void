package content.skill.dungeoneering

import content.quest.instance
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

data class DungeonRoom(val tile: Tile, val isCritical: Boolean) {
    var type: DungeonRoomType = DungeonRoomType.Normal
    val keys = mutableListOf<String>()
    val doors = arrayOfNulls<DungeonDoor>(4)
    val adjacentRooms = arrayOfNulls<DungeonRoom>(4)
    var parent: DungeonRoom? = null

    var open: Boolean = false
    var zone: Zone? = null
    var rotation: Int = 0

    fun open(player: Player, dungeon: DungeonMap) {
        val zone = zone ?: return
        val origin = player.instance()?.tile?.zone ?: return
        if (open) {
            return
        }
        open = true
        val target = origin.add(tile.x * 2, tile.y * 2)
        for (key in keys) {
            FloorItems.add(target.tile.add(8, 8), key)
        }
        val zones = get<DynamicZones>()
        for (sx in 0..1) {
            for (sy in 0..1) {
                // Calculate the target zone offset (tx, ty) based on CW rotation
                val tx = when (rotation) {
                    0 -> sx
                    1 -> 1 - sy
                    2 -> 1 - sx
                    3 -> sy
                    else -> sx
                }
                val ty = when (rotation) {
                    0 -> sy
                    1 -> sx
                    2 -> 1 - sy
                    3 -> 1 - sx
                    else -> sy
                }
                val clientRotation = (4 - rotation) % 4
                zones.copy(zone.add(sx, sy), target.add(tx, ty), clientRotation)
            }
        }
        val theme = dungeon.theme
        for ((i, door) in doors.withIndex()) {
            if (door == null) {
                continue
            }
            val dir = Direction.westClockwise[i]
            val delta = when (dir) {
                Direction.WEST -> Delta(0, 7)
                Direction.NORTH -> Delta(7, 15)
                Direction.EAST -> Delta(15, 7)
                else -> Delta(7, 0)
            }
            val tile = target.tile.add(delta)
            val id = when (door) {
                is DungeonDoor.Blocked -> {
                    val skill = door.skill.name.lowercase()
                    val skillDoor = Tables.obj("skill_doors.$skill.id").replace("_frozen", "_$theme")
                    if (Tables.bool("skill_doors.$skill.in_front")) {
                        GameObjects.add(skillDoor, tile.add(dir.inverse()), rotation = i)
                        "door_$theme"
                    } else {
                        skillDoor
                    }
                }
                DungeonDoor.Guardian -> "guardian_door_$theme"
                is DungeonDoor.Locked -> {
                    GameObjects.add(door.key.replace("_key", "_door"), tile.add(dir.inverse()), rotation = i)
                    "${door.key.replace("_key", "_door")}_$theme"
                }
                DungeonDoor.Normal -> "door_$theme"
            }
            GameObjects.add(id, tile, rotation = i)
        }
    }
}

internal val Direction.roomIndex: Int
    get() = when (this) {
        Direction.WEST -> 0
        Direction.NORTH -> 1
        Direction.EAST -> 2
        Direction.SOUTH -> 3
        else -> -1
    }
