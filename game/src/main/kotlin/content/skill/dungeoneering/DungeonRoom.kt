package content.skill.dungeoneering

import com.github.michaelbull.logging.InlineLogger
import content.quest.instance
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.*
import world.gregs.voidps.type.area.Rectangle

data class DungeonRoom(val tile: Tile, val isCritical: Boolean) {
    var type: DungeonRoomType = DungeonRoomType.Normal
    val keys = mutableListOf<String>()
    val doors = arrayOfNulls<DungeonDoor>(4)
    val adjacentRooms = arrayOfNulls<DungeonRoom>(4)
    var parent: DungeonRoom? = null

    var name: String? = null
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
        val zones = get<DynamicZones>()
        for (sx in 0..1) {
            for (sy in 0..1) {
                // Calculate the target zone offset (tx, ty) based on CW rotation
                val tx = DungeonMap.rotateX(sx, sy, rotation, 1)
                val ty = DungeonMap.rotateY(sx, sy, rotation, 1)
                val clientRotation = (4 - rotation) % 4
                zones.copy(zone.add(sx, sy), target.add(tx, ty), clientRotation)
            }
        }
        // Spawn keys
        spawnKeys(dungeon)
        val complexity = player["dungeoneering_party_complexity", 1]
        val floor = player["dungeoneering_party_floor", 1]
        DungeonTableItems.spawn(complexity, dungeon, dungeon.skills, dungeon.playerCount)
        DungeonNPCs.spawn(dungeon, this, floor, complexity)
        spawnDoors(target, dungeon.theme)
    }

    private fun spawnKeys(dungeon: DungeonMap) {
        val keySpots = findObjects(dungeon, setOf("rand_invis_key_location"))
        if (keySpots.isEmpty()) {
            logger.warn { "Unable to find key tile for $zone $name" }
            return
        }
        for (key in keySpots) {
            key.remove()
        }
        for (key in keys) {
            FloorItems.add(keySpots.random(random).tile, key)
        }
    }

    fun findObjects(dungeon: DungeonMap, ids: Set<String>): List<GameObject> {
        val objects = mutableListOf<GameObject>()
        for (x in 0 until 16) {
            for (y in 0 until 16) {
                val tile = dungeon.tile(this, x, y)
                val obj = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: continue
                if (ids.contains(obj.id)) {
                    objects.add(obj)
                }
            }
        }
        return objects
    }

    private fun spawnDoors(target: Zone, theme: String) {
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

    companion object {
        private val logger = InlineLogger()
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

internal fun Character.dungeonRoomBounds(): Rectangle {
    val start = Tile(tile.x / 16 * 16 + 1, tile.y / 16 * 16 + 1)
    return Rectangle(start, 14, 14)
}
