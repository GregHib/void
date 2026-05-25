package content.skill.dungeoneering

import world.gregs.voidps.type.Direction

class DungeonMap(
    val width: Int,
    val height: Int,
    val grid: Array<DungeonRoom?>
) {

    private val Direction.index: Int
        get() = when (this) {
            Direction.WEST -> 0
            Direction.NORTH -> 1
            Direction.EAST -> 2
            Direction.SOUTH -> 3
            else -> -1
        }

    fun prettyPrint() {
        val charWidth = 2 * width + 1
        val charHeight = 2 * height + 1
        val renderGrid = Array(charHeight) { CharArray(charWidth) { ' ' } }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val room = grid[y * width + x] ?: continue
                val cx = 2 * x + 1
                val cy = 2 * (height - 1 - y) + 1

                // Draw corners
                renderGrid[cy - 1][cx - 1] = '+'
                renderGrid[cy - 1][cx + 1] = '+'
                renderGrid[cy + 1][cx - 1] = '+'
                renderGrid[cy + 1][cx + 1] = '+'

                // Draw room centre
                renderGrid[cy][cx] = when (room.type) {
                    DungeonRoomType.Base -> 'S'
                    DungeonRoomType.Boss -> 'B'
                    DungeonRoomType.Puzzle -> 'P'
                    DungeonRoomType.Normal -> {
                        if (room.keyIds.isNotEmpty()) {
                            val firstKey = room.keyIds.first()
                            getKeyChar(firstKey)
                        } else {
                            '.'
                        }
                    }
                }

                // Draw doors or walls
                for (dir in Direction.westClockwise) {
                    val wx = cx + dir.delta.x
                    val wy = cy - dir.delta.y
                    val door = room.doors[dir.index]

                    if (door == null) {
                        renderGrid[wy][wx] = if (dir == Direction.NORTH || dir == Direction.SOUTH) '-' else '|'
                        continue
                    }

                    if (door is DungeonDoor.Locked) {
                        renderGrid[wy][wx] = ('A'.code + door.depth).toChar()
                        continue
                    }

                    // Check if traversing outwards (from parent to child)
                    val adjacentRoom = room.adjacentRooms[dir.index]!!
                    val isOutward = adjacentRoom.parent == room

                    if (isOutward && door is DungeonDoor.Guardian) {
                        renderGrid[wy][wx] = 'G'
                    } else if (isOutward && door is DungeonDoor.Blocked) {
                        renderGrid[wy][wx] = 'K'
                    } else {
                        renderGrid[wy][wx] = ' ' // Standard open passage
                    }
                }
            }
        }

        // Print ASCII Map
        for (row in renderGrid) {
            println(row.joinToString(""))
        }

        // Print Key inventory
        println("\nRoom Inventory:")
        var keysFound = false
        for (y in height - 1 downTo 0) {
            for (x in 0 until width) {
                val room = grid[y * width + x] ?: continue
                if (room.keyIds.isNotEmpty()) {
                    keysFound = true
                    val keysStr = room.keyIds.map { getKeyChar(it) }.joinToString(", ")
                    val typeStr = when (room.type) {
                        DungeonRoomType.Base -> "Start Room"
                        DungeonRoomType.Boss -> "Boss Room"
                        DungeonRoomType.Puzzle -> "Puzzle Room"
                        DungeonRoomType.Normal -> if (room.isCritical) "Critical Normal" else "Bonus Normal"
                    }
                    val zoneStr = room.zone?.let { " [Template at: (${it.x}, ${it.y}), Rot: ${room.rotation * 90}°]" } ?: ""
                    println("  Room at ($x, $y) [$typeStr]$zoneStr contains key(s): [ $keysStr ]")
                }
            }
        }
        if (!keysFound) println("  No keys placed.")
    }

    private fun getKeyChar(keyId: Int): Char {
        return ('a'.code + keyId).toChar()
    }

}