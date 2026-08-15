package content.skill.dungeoneering

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.drop.DropTables

class Dungeoneering(val dropTables: DropTables) : Script {
    init {
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

    companion object {
        fun rotateX(x: Int, y: Int, rotation: Int, size: Int) = when (rotation) {
            0 -> x
            1 -> size - y
            2 -> size - x
            3 -> y
            else -> x
        }

        fun rotateY(x: Int, y: Int, rotation: Int, size: Int) = when (rotation) {
            0 -> y
            1 -> x
            2 -> size - y
            3 -> size - x
            else -> y
        }
    }
}
