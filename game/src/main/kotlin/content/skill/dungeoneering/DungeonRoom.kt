package content.skill.dungeoneering

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.quest.instance
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
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

        // cmb125 (135) f1:c6:f2p
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
        // 4..16

        // lvl 32
        // f1:c6:f2p
        // 1
        // 14
        // 1, 1
        // 5
        // 1..14

        // 32 f1:c1:f2p
        // 7, 2, 1 - 10
        // 14, 5 - 19
        // 5
        // 7, 1, 1 - 9
        // 14
        // 1, 14 - 15
        // icy bones 16
        // 5..19

        // 32 f2:c1
        // 1, 1, 7 - 9
        // 4, 14, 1 - 19
        // 7, 4 - 11
        // 10
        // 14, 14 - 28
        // glu beh 1
        // 9..28

        // 32 f3:C1
        // 1, 7 - 8
        // 1, 1 - 2
        // 1, 3 - 4
        // 10
        // 2, 7 - 9
        // 14, 14 - 28
        // lu ice 9
        // 2..28

        // 32 f4:c1
        // 4, 14 - 18
        // 7, 1 - 8
        // 28
        // 14, 14 - 28
        // 14, 5, 7 - 26
        // lu ice 9
        // 8..28

        // 33 f5:c1
        // 4, 14 - 18
        // 5
        // 1
        // 1
        // 14, 2, 7 - 23
        // 1, 14, 1 - 16
        // lu ice 9
        // 1..23

        // 33 f6:c1
        // 10
        // 4, 14 - 18
        // 1, 1 - 2
        // 1, 7, 4 - 12
        // icy bones 16
        // 2..18

        // 33 f6:c1
        // 14, 4, 1 - 19
        // 14
        // 4, 2, 1 - 7
        // 4, 14 - 18
        // 5
        // 1
        // lu ice 9
        // 1..19

        // 33 f7:c1
        // 7, 7, 1 - 15
        // 14, 7 - 21
        // 1, 1, 1 - 3
        // 14, 1 - 15
        // 1, 4, 1 - 6
        // plane freezer lakh 1
        // 3..21

        // 34 f8:c1
        // 4, 1 - 5
        // 5
        // 4, 1, 14 - 19
        // 2, 1, 1 - 4
        // 7, 1, 4 - 12
        // plane freezer lakh 1
        // 4..19

        // 34 f8:c1
        // 10
        // 5
        // 1
        // 14
        // 1
        // plane freezer lakh 1
        // 1..14

        // 34 f9:c1
        // 7, 1, 1 - 9
        // 1, 1, 1 - 3
        // 1, 7, 7 - 15
        // 1, 4 - 5
        // tok bloodchiller 1
        // 3..15

        // 34 f9:c1
        // 25
        // 14, 7, 7 - 28
        // 1, 1, 1 - 3
        // 1, 7, 4 - 12
        // tok bloodchiller 1
        // 3..28

        // 34 f10:c1
        // 4, 1 - 5
        // 2, 1 - 3
        // 5, 2, 1 - 8
        // 14, 2 - 16
        // 1, 4 - 5
        // 1, 1, 1 - 3
        // tok bloodchiller 1
        // 3..16

        // 34 f10:c6
        // 14 (frozen figure)
        // 10, 1, 1 - 12
        // 14, 1, 1 - 16
        // 0
        // 7, 7, 7 - 21
        // 0
        // 5
        // 7, 4 - 11
        // 5
        // 10, 1 - 11
        // 5, 1 - 6
        // tok blood chiller 11
        // 5..21

        // 34 f11:c1
        // 1, 2, 1 - 4
        // 2, 1, 1 - 4
        // 5
        // 14, 3 - 17
        // 3, 4 - 7
        // 1
        // lu icefiend 9
        // 1..17

        // 34 f12:c1
        // 1, 1, 22 - 24
        // 1, 4, 1 - 6
        // 4, 5, 1 - 10
        // 3, 7, 14 - 24
        // 14, 1 - 15
        // 28
        // skin weaver (18)
        // 6..28

        // 34 f13:c6
        // 1
        // 47, 47 (two joined ghosts?)
        // 4, 18 - 22
        // 18
        // 1, 18, 14 - 33
        // 5, 14 - 19
        // 28, 1, 28 - 57
        // 1
        // 30, 28 - 58
        // 1, 22, 28 - 51
        // 31, 10, 1 - 42
        // bulkwark 7
        // 1..57

        // 34 f14:c6
        // 36
        // 1
        // 36, 5 - 41
        // 14, 30 - 44
        // 14, 36 - 50
        // 1 (construct puzzle)
        // 28
        // 18, 30 - 48
        // 43
        // 30, 4 - 34
        // 56
        // 14, 1 - 15
        // 14, 4, 1 - 19
        // 1.. 50


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
        // 1..24

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

        // 125 f20:c1:f2p
        // 2, 40, 2 - 44
        // 21, 14 - 35
        // 50
        // 1, 19, 1 - 21
        // 28, 19 - 47
        // 48
        // Lexus runewrite (7)
        // 21..48

        // 125 f20:c6:f2p
        // 1, 10, 25 - 36
        // 25, 10, 19 - 56
        // 1, 56 - 57
        // 86
        // lexus runewrite (7)
        // 56, 35 - 91
        // 14, 47 - 61
        // 36, 65 - 101
        // 14, 47 - 61
        // 57, 27 - 84
        // 84
        // 36..101

        // 125 f25:c1:f2p
        // 65
        // 1
        // 1, 36, 10 - 47
        // 14
        // har'lak rift 7

        // 125 f25:c1
        // 35, 19, 15 - 69
        // 43
        // 31, 19 - 50
        // 19, 19, 1 - 39
        // 1, 21, 19 - 41
        // rammnaut 7
        // 39..69

        // 125 f25:c6:f2p
        // 10, 35, 1 (follow leader puzzle) - 46
        // 56
        // 1, 42 - 43
        // 84
        // 84
        // 56 (ferret pressure pad puzzle)
        // 35, 56 - 91
        // 50, 50 - 100
        // 36, 36 - 72
        // lex rr 7
        // 1..100


        // Complexity 1
        // floor 35 = 90
        // floor 1 = 45
        // floor 1 = 16
        // floor 20 = 50
        // Complexity 6
        // floor 35 = 138  // 48
        // floor 1 = 85 // +40
        // floor 1 = 23 // 7
        // floor 20 = 101


        // level 125

        // f25:c6 1..100
        // f20:c6 36..101
        // f1:c6 1..84
        // f35:c6 49..138

        // f1:c1 6..44
        // f20:c1 21..48
        // f25:c1 39..69
        // f25:c1 1..65
        // f35:c1 19..89


        // c6 = +40 @ 125cmb

        // c1 = 45..90

        // level 3

        // f1:c1 1..16
        // f1:c6 1..24
        // c6 = +8 @ 32cmb

        // c6 = cmb 1..45


        // 4/5 chance of spawning npcs (required by guardian rooms)
        // party
        // combat level is split between number of spawns
        // complexity changes max combat level
        // puzzle rooms can limit max combat level
        // more players more combat to split
        //

        // lvl4 + 125 f1:c1:d2
        // 1, 7, 1 - 9
        // 15, 1 - 16
        // 10, 1, 5 - 16
        // 10, 10, 1 - 21
        // icy bones 33
        // 9..21

        // lvl4 + 125 f2:c1:d2
        // 4, 5, 5 - 14
        // 5, 7, 10 - 22
        // 18
        // astea frostweb 32
        // 14..22

        // lvl4 + 125 f3:c1:d2
        // 1, 5, 14 - 20
        // 14
        // 5, 25, 14 - 44
        // 28
        // 42
        // glut beh 9
        // 14..44

        // lvl4 + 125 f3:c1:d2
        // 1, 7, 1 - 9
        // 18, 35, 1 - 54
        // 1, 28, 25, 10 - 64
        // 48
        // 5, 28
        // astea 32
        // 9..64

        // lvl4 + 125 f4:c1:d1
        // 1, 1, 1
        // 1, 1, 1
        // 1
        // 1, 1
        // 1, 1, 1
        // ice fiend 1
        // 215

        val complexity = player["dungeoneering_complexity", 0]
        if (random.nextInt(5) != 0 || doors.any { it is DungeonDoor.Guardian }) {
            var total = 0

            val members = player.dungeonMembers.sortedBy { it.combatLevel }.take(dungeon.playerCount)
            val average = members.sumOf { it.combatLevel } / members.size

            val combatLevel = player.dungeonMembers.maxOf { it.combatLevel }

            // c1 up to 30, c6 up to 60 @ 34cmb floors 10+

            // 60 @138
            // c1 @ 64cmb? 64..
            // c1 @ 3cmb 3..?
            // c1 @ 32-33cmb 28..88?
            // floor, 1..60, 50..135 c1 @ 125cmb
            // 22 diff between 125 and 32
            // 25 diff between 32 and 3
//            Interpolation.lerp(floor, 1..60, 50..135)
            total += complexityBonus(combatLevel, complexity)
            val max = player.dungeonMembers.maxOf { it.combatLevel / 2 }

            total.coerceAtLeast(1)
        }

        /*
            combat:floor:complexity-max level seen
            125cmb:f1:c6-84
            125cmb:f35:c6-138
            125cmb:f1:c1-44
            125cmb:f35:c1-89
            32cmb:f1:c1-16
            32cmb:f1:c6-14
            32cmb:f1:c1-19
            32cmb:f2:c1-28
            32cmb:f3:c1-28
            32cmb:f4:c1-28
            33cmb:f5:c1-23
            33cmb:f6:c1-18
            33cmb:f6:c1-19
            33cmb:f7:c1-21
            34cmb:f8:c1-19
            34cmb:f8:c1-14
            34cmb:f9:c1-15
            34cmb:f10:c1-16
            34cmb:f10:c1-21
            34cmb:f11:c1-17
            34cmb:f12:c1-28
            34cmb:f13:c6-57
            34cmb:f14:c6-50
            32cmb:f1:c6-24
            3cmb:f1:c1-1
            3cmb:f1:c1-1
            3cmb:f1:c6-1
            125cmb:f20:c1-48
            125cmb:f20:c6-101
            125cmb:f25:c1-65
            125cmb:f25:c1-69
            125cmb:f25:c6-100
            4+125cmb:f1:c1-21
            4+125cmb:f2:c1-32
            4+125cmb:f3:c1-44
            4+125cmb:f3:c1-64
         */
    }

    /**
     * Complexity bonus based off a sample of rs3 low (c1) and high (c6) dungeons
     * Max combat level of monsters increases by approx: +40 @ 125 combat, +8 @ 32 combat, +0 @ 3 combat
     */
    private fun complexityBonus(combatLevel: Int, complexity: Int): Int = Interpolation.lerp(
        value = combatLevel,
        inRange = 3..138,
        result = 0..when (complexity) {
            2 -> 5
            3 -> 15
            4 -> 25
            5 -> 35
            6 -> 45
            else -> 0
        }
    )
}

internal val Direction.roomIndex: Int
    get() = when (this) {
        Direction.WEST -> 0
        Direction.NORTH -> 1
        Direction.EAST -> 2
        Direction.SOUTH -> 3
        else -> -1
    }
