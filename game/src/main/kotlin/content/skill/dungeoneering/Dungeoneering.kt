package content.skill.dungeoneering

import content.quest.smallInstance
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class Dungeoneering : Script {
    init {
        adminCommand("start_dungeon", intArg("floor", optional = true), stringArg("size", autofill = setOf("small", "medium", "large"), optional = true), intArg("complexity", optional = true)) { args ->
//            setRandom(Random(0L))
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
            startRoom.open(this, dungeon)
            dungeon.players.add(index)
            var tile = startRoom.tile
            delay(1) // Delay to avoid ghosting # 1000
            tele(instance.tile.add(tile.x * 16 + 8, tile.y * 16 + 8))

            spawnItems(instance.tile, 0, dungeon)
        }

        adminCommand("dungeon") {
            val dungeon = dungeonMap ?: return@adminCommand
            dungeon.prettyPrint(6) { renderGrid ->
                for (row in renderGrid) {
                    message(
                        row.joinToString("")
                            .replace(" ", "  ")
                            .replace("|  ", "|    ")
                            .replace("  |", "    |")
                            .replace("Crit", "Crit ")
                            .replace("   Base ", "Base")
                            .replace(" Norm ", "Norm"),
                        ChatType.Console,
                    )
                }

                message("\nRoom Keys:", ChatType.Console)
                var keysFound = false
                for (y in dungeon.height - 1 downTo 0) {
                    for (x in 0 until dungeon.width) {
                        val room = dungeon.grid[y * dungeon.width + x] ?: continue
                        if (room.keys.isNotEmpty()) {
                            keysFound = true
                            val keysStr = room.keys.joinToString(", ") { DungeonMap.getKeyAbbrev(it).lowercase() }
                            val typeStr = when (room.type) {
                                DungeonRoomType.Base -> "Start Room"
                                DungeonRoomType.Boss -> "Boss Room"
                                DungeonRoomType.Puzzle -> "Puzzle Room"
                                DungeonRoomType.Normal -> if (room.isCritical) "Critical Normal" else "Bonus Normal"
                            }
                            message("  Room at ($x, $y) [$typeStr] contains key(s): [ $keysStr ]", ChatType.Console)
                        }
                    }
                }
                if (!keysFound) {
                    message("  No keys placed.", ChatType.Console)
                }
            }
        }

        adminCommand("unlock_dungeon") {
            val dungeon = dungeonMap ?: return@adminCommand
            for (room in dungeon.grid) {
                if (room == null || room.open) {
                    continue
                }
                room.open(this, dungeon)
            }
        }
    }

    private val fish = arrayOf("heim_crab", "red_eye", "dusk_eel", "giant_flatfish", "short_finned_eel", "web_snipper", "bouldabass", "salve_eel", "blue_crab", "cave_moray")

    private fun spawnItems(instance: Tile, complexity: Int, dungeon: DungeonMap) {
        val items = mutableListOf<ItemDrop>()
        val startRoom = dungeon.start()
        val tile = instance.add(startRoom.tile.x * 16, startRoom.tile.y * 16)
        val teamCount = 1
        val fishCount = 7 + teamCount * 2
        val highestConstitution = 10
        repeat(fishCount) {
            val index = random.nextInt(0, highestConstitution / 10)
            val fish = fish[index]
            items.add(ItemDrop(fish))
        }
        // TODO highest levels
        if (dungeon.traverse { _, door, _ -> door is DungeonDoor.Blocked && door.skill == Skill.Mining }.isNotEmpty()) { // TODO mining puzzles
            // TODO pickaxe
        }
        if (dungeon.traverse { _, door, _ -> door is DungeonDoor.Blocked && door.skill == Skill.Woodcutting }.isNotEmpty()) { // TODO mining puzzles
            // TODO hatchet
        }
        // TODO dragonfire shield if dragon spawn
        items.add(ItemDrop("rusty_coins", 490..510))
        val tiles = listOf(
            Delta(x = 10, y = 14),
            Delta(x = 11, y = 14),
            Delta(x = 14, y = 11),
            Delta(x = 14, y = 10),
            Delta(x = 9, y = 11),
            Delta(x = 10, y = 11),
            Delta(x = 11, y = 11),
            Delta(x = 9, y = 10),
            Delta(x = 10, y = 10),
            Delta(x = 11, y = 10)
        )
        for (drop in items) {
            val delta = tiles.random(random)
            val tx = rotateX(delta.x, delta.y, startRoom.rotation, 15)
            val ty = rotateY(delta.x, delta.y, startRoom.rotation, 15)
            val item = drop.toItem()
            FloorItems.add(tile.add(tx, ty), item.id, item.amount)
        }
    }

    private fun rotateX(x: Int, y: Int, rotation: Int, size: Int) = when (rotation) {
        0 -> x
        1 -> size - y
        2 -> size - x
        3 -> y
        else -> x
    }

    private fun rotateY(x: Int, y: Int, rotation: Int, size: Int) = when (rotation) {
        0 -> y
        1 -> x
        2 -> size - y
        3 -> size - x
        else -> y
    }
}
