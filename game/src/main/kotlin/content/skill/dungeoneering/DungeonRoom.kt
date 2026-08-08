package content.skill.dungeoneering

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.maxSkills
import content.quest.instance
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.type.random
import kotlin.random.nextInt

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
        val zones = get<DynamicZones>()
        for (sx in 0..1) {
            for (sy in 0..1) {
                // Calculate the target zone offset (tx, ty) based on CW rotation
                val tx = Dungeoneering.rotateX(sx, sy, rotation, 1)
                val ty = Dungeoneering.rotateY(sx, sy, rotation, 1)
                val clientRotation = (4 - rotation) % 4
                zones.copy(zone.add(sx, sy), target.add(tx, ty), clientRotation)
            }
        }

        val keys = keys.toMutableList()
        val keyTiles = mutableListOf<Tile>()
        val npcSpawns = mutableListOf<Rectangle>()
        for (x in 0 until 16) {
            for (y in 0 until 16) {
                val tile = target.tile.add(x, y)
                val obj = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: continue
                when (obj.id) {
                    "rand_invis_key_location" -> {
                        keyTiles.add(tile)
                        obj.remove()
                    }
                    "rand_npc_spawn_1x1" -> npcSpawns.add(Rectangle(obj.tile, 1, 1))
                    "rand_npc_spawn_2x2" -> npcSpawns.add(Rectangle(obj.tile, 2, 2))
                    "rand_npc_spawn_3x3" -> npcSpawns.add(Rectangle(obj.tile, 3, 3))
                    "rand_npc_spawn_4x4" -> npcSpawns.add(Rectangle(obj.tile, 4, 4))
                    "rand_npc_spawn_5x5" -> npcSpawns.add(Rectangle(obj.tile, 5, 5))
                }
            }
        }
        // Spawn keys
        for (key in keys) {
            FloorItems.add(keyTiles.random(random), key)
        }
        spawnNpcs(player, dungeon, npcSpawns)

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

    private fun spawnNpcs(player: Player, dungeon: DungeonMap, npcSpawns: MutableList<Rectangle>) {
        if (npcSpawns.isEmpty()) {
            return
        }
        if (type == DungeonRoomType.Base || type == DungeonRoomType.Boss) {
            return
        }
        // https://runescape.wiki/w/Dungeoneering/Monsters#Overview
        val spawnCount = random.nextInt(0..dungeon.playerCount + 3)
        if (spawnCount == 0) {
            return
        }


        "You need some construct parts to attempt a repair."
        "You take a lump of magic stone from the crate."
        "You carve the block into a part of the stone construct."
        "You imbune the construct part with rune energy."
        "You attack the missing part to the construct."
        "You charge the construct with magical energy and it springs to life."
        "You must be on a members' world to access this feature." // F2p renew sum obl points

        // cmb125 f1:c6
        // 14
        // 1
        // 14,42 - 56
        // 0
        // 12,28 - 40
        // 0
        // 31, 12, 5 - 48
        // 56, 28 - 84
        // 1, 1 (spike puzzle)
        // 1, 1 (non crit)
        // icy bones 57
        // 1..84

        // f35:c6:f2p
        // 48, 1 - 49
        // 2, 37, 42 - 81
        // 93 (damaged construct)
        // 94
        // 96
        // balak the pummler 91
        // 18, 25, 33 - 76
        // 82, 56 - 138
        // 43, 33 - 76
        // 49..138

        // f1:c1:f2p
        // 5, 1, 1 - 7
        // 5, 1 - 6
        // 5, 28, 1 - 34
        // 15, 1 - 16
        // 35, 1 - 36
        // 5, 4, 35 - 44
        // glut beh 18
        // 6..44

        // f35:c1:f2p
        // 1, 35, 47 - 83
        // 47, 42 - 89
        // 70
        // 4, 14, 14 - 32
        // 1, 18 - 19
        // 1, 37 - 38
        // balak pummel 91
        // 19..89

        // lvl 32
        // f1:c1:f2p
        // 1, 14 - 15
        // 1, 5 - 6
        // 1, 1, 14 - 16
        // 4
        // 2, 7, 7 - 16
        // astea frostweb 16

        // lvl 32
        // f1:c6:f2p
        // 1
        // 14
        // 1, 1
        // 5

        // lvl 32
        // f1:c6:f2p
        // 1, 4 - 5
        // 10, 10 - 20
        // 1
        // 19
        // 1, 1, 7 - 9
        // 10, 14 - 24
        // 1, 1, 1 - 3
        // 1, 1, 1 - 3
        // astea frostweb 23

        // lvl 3
        // f1:c1:f2p
        // 1, 1
        // 1
        // 1
        // 1, 1, 1

        // lvl 3
        // f1:c1:f2p
        // 1
        // 1
        // 1
        // 1
        // astea frostweb 1

        // lvl 3
        // f1:c6:f2p
        // 1, 1
        // 1, 1
        // 1
        // 1
        // 1, 1, 1
        // 1, 1
        // 1, 1
        //



        // Complexity 1
        // floor 35 = 90
        // floor 1 = 45
        // floor 1 = 16
        // Complexity 6
        // floor 35 = 138  // 48
        // floor 1 = 85 // +40
        // floor 1 = 23 // 7


        // 4/5 chance of spawning npcs (required by guardian rooms)
        // party
        // combat level is split between number of spawns
        // complexity changes max combat level
        // puzzle rooms can limit max combat level
        // more players more combat to split
        //

        if (random.nextInt(5) != 0 || doors.any { it is DungeonDoor.Guardian }) {
            val total = 0
            dungeon.playerCount
            val max = player.dungeonMembers.maxOf { it.combatLevel } - 20 // If low complexty and f2p??
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
