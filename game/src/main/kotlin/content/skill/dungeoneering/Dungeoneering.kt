package content.skill.dungeoneering

import content.quest.instance
import content.quest.smallInstance
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.random.Random

class Dungeoneering : Script {
    fun Player.dungeonRoom(tile: Tile): DungeonRoom? {
        val instance = instance() ?: return null
        val origin = tile.delta(instance.tile)
        val room = origin.room
        val delta = origin.minus(room)
        return dungeonMap?.room(delta.x, delta.y)
    }

    val Tile.room: Tile
        get() = Tile(x / 16, y / 16)

    val Delta.room: Delta
        get() = Delta(x / 16, y / 16)

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

        adminCommand("dungeon", intArg("floor", optional = true), stringArg("size", autofill = setOf("small", "medium", "large"), optional = true), intArg("complexity", optional = true)) { args ->
            setRandom(Random(0L))
            println("Generating dungeon...")
            val floor = args.getOrNull(0)?.toInt() ?: 1
            val size = DungeonSize.valueOf((args.getOrNull(1) ?: "small").toTitleCase())
            val complexity = args.getOrNull(2)?.toInt() ?: 1
            val generator = DungeonGenerator(
                size = size,
                floor = floor,
                complexity = complexity,
            )
            set("show_daemonheim_map", true)
            val skills = DungeonDoor.Blocked.skills.associateWith { levels.getMax(it) }
            message("")
            message("- Welcome to Daemonheim -")
            message("Floor <purple>$floor</col>    Complexity <purple>$complexity")
            message("Dungeon Size: <purple>$size")
            message("Party Size:Difficulty <purple>1:1")
            message("<purple>Guide Mode OFF")
            message("")
            val start = System.currentTimeMillis()
            val dungeon = generator.generate(skills)
            println("Took ${System.currentTimeMillis() - start}ms")

            set("dungeon", dungeon)

            dungeon.prettyPrint()

            val instance = smallInstance(logout = false)
            dungeon.region = instance
            val startRoom = dungeon.start()
            openRoom(this, startRoom, dungeon)
            dungeon.players.add(index)
            var tile = startRoom.tile
            tele(instance.tile.add(tile.x * 16 + 8, tile.y * 16 + 8))
        }

        adminCommand("unlock_dungeon") {
            val dungeon = dungeonMap ?: return@adminCommand
            for (room in dungeon.grid) {
                if (room == null || room.open) {
                    continue
                }
                openRoom(this, room, dungeon)
            }
        }
    }

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
        openRoom(this, adj, dungeon)
    }

    private fun openRoom(player: Player, room: DungeonRoom, dungeon: DungeonMap) {
        val zone = room.zone ?: return
        val origin = player.instance()?.tile?.zone ?: return
        if (room.open) {
            return
        }
        room.open = true
        val target = origin.add(room.tile.x * 2, room.tile.y * 2)
        for (key in room.keys) {
            FloorItems.add(target.tile.add(8, 8), key)
        }
        val zones = get<DynamicZones>()
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
        val theme = dungeon.theme
        for ((i, door) in room.doors.withIndex()) {
            if (door == null) {
                continue
            }
            val dir = Direction.westClockwise[i]
            val delta = toDelta(dir)
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

    private fun toDirection(delta: Delta): Direction = when (delta) {
        Delta(0, 7) -> Direction.WEST
        Delta(7, 15) -> Direction.NORTH
        Delta(15, 7) -> Direction.EAST
        else -> Direction.SOUTH
    }

    private fun toDelta(direction: Direction): Delta = when (direction) {
        Direction.WEST -> Delta(0, 7)
        Direction.NORTH -> Delta(7, 15)
        Direction.EAST -> Delta(15, 7)
        else -> Delta(7, 0)
    }
}
