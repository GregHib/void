package content.skill.dungeoneering

import content.quest.instance
import content.quest.smallInstance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.random.Random

class Dungeoneering : Script {
    init {
        objectSpawn("occult_door_normal") {
            println("Spawned ${this.tile}")
        }

        objectOperate("Enter", "occult_door_normal") { (target) ->
            val dungeon: DungeonMap = get("dungeon") ?: return@objectOperate
            val instance = instance() ?: return@objectOperate
            val delta = target.tile.delta(instance.tile)
            val rx = delta.x / 16
            val ry = delta.y / 16
            val dx = delta.x - (rx * 16)
            val dy = delta.y - (ry * 16)
            val direction = toDirection(Delta(dx, dy))
            val room = dungeon.room(rx, ry)
//            val target = dungeon.room(rx + direction.delta.x, ry + direction.delta.y)!!
            tele(tile.add(direction).add(direction).add(direction))
        }

        adminCommand("dungeon") {
//            setRandom(Random(0L))
            println("Generating dungeon...")
            val floor = 60
            val size = DungeonSize.Small
            val complexity = 1
            val generator = DungeonGenerator(
                size = size,
                floor = floor,
                complexity = complexity,
            )
            val skills = DungeonDoor.Blocked.skills.associateWith { levels.getMax(it) }
            message("")
            message("- Welcome to Daemonheim -")
            message("Floor <purple>$floor</col>    Complexity <purple>$complexity")
            message("Dungeon Size: <purple>${size}")
            message("Party Size:Difficulty <purple>1:1")
            message("<purple>Guide Mode OFF")
            message("")
            val start = System.currentTimeMillis()
            val dungeon = generator.generate(skills)
            println("Took ${System.currentTimeMillis() - start}ms")


            set("dungeon", dungeon)

            dungeon.prettyPrint()

            val instance = smallInstance()
            val zones = get<DynamicZones>()
            val origin = instance.tile.zone
            var tile = Tile.EMPTY
            val theme = dungeon.theme
            for (room in dungeon.grid) {
                if (room == null) {
                    continue
                }
                if (room.type == DungeonRoomType.Base) {
                    tile = room.tile
                }
                val zone = room.zone ?: continue
                val target = origin.add(room.tile.x * 2, room.tile.y * 2)
                if (room.keys.isNotEmpty()) {
                    for (key in room.keys) {
                        FloorItems.add(target.tile.add(8, 8), key)
                    }
                }
                // Iterate through the 2x2 zones of the room template
                for (sx in 0..1) {
                    for (sy in 0..1) {
                        // Calculate the target zone offset (tx, ty) based on CW rotation
                        val tx = when (room.rotation) {
                            0 -> sx
                            1 -> 1 - sy
                            2 -> 1 - sx
                            3 -> sy
                            else -> sx
                        }
                        val ty = when (room.rotation) {
                            0 -> sy
                            1 -> sx
                            2 -> 1 - sy
                            3 -> 1 - sx
                            else -> sy
                        }
                        val clientRotation = (4 - room.rotation) % 4
                        zones.copy(zone.add(sx, sy), target.add(tx, ty), clientRotation)
                    }
                }
                // TODO don't spawn two keys and two doors
                //      Boss doors take priority over key doors?
                for ((i, door) in room.doors.withIndex()) {
                    if (door == null) {
                        continue
                    }
                    val dir = Direction.westClockwise[i]
                    val delta = toDelta(dir)
                    val tile = target.tile.add(delta)
                    val id = when (door) {
                        is DungeonDoor.Blocked -> {
                            val skillDoor = Tables.obj("skill_doors.${door.skill.name.lowercase()}.id").replace("_frozen", "_${theme}")
                            if (door.skill == Skill.Runecrafting) {
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
            tele(instance.tile.add(tile.x * 16 + 8, tile.y * 16 + 8))
        }
    }

    private fun toDirection(delta: Delta) : Direction = when (delta) {
        Delta(0, 7) -> Direction.WEST
        Delta(7, 15) -> Direction.NORTH
        Delta(15, 7) -> Direction.EAST
        else -> Direction.SOUTH
    }

    private fun toDelta(direction: Direction) : Delta = when (direction) {
        Direction.WEST -> Delta(0, 7)
        Direction.NORTH -> Delta(7, 15)
        Direction.EAST -> Delta(15, 7)
        else -> Delta(7, 0)
    }
}
