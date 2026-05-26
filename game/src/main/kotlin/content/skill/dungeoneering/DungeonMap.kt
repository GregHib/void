package content.skill.dungeoneering

import world.gregs.voidps.type.Direction

class DungeonMap(
    val width: Int,
    val height: Int,
    val grid: Array<DungeonRoom?>,
) {

    private val Direction.index: Int
        get() = when (this) {
            Direction.WEST -> 0
            Direction.NORTH -> 1
            Direction.EAST -> 2
            Direction.SOUTH -> 3
            else -> -1
        }

    /**
     * Prints the map dynamically.
     * @param innerRoomWidth The inside width of each room in characters.
     */
    fun prettyPrint(innerRoomWidth: Int = 9) {
        val halfWidth = innerRoomWidth / 2
        val wallOffset = halfWidth + 1
        val stepX = 2 * halfWidth + 2
        val stepY = 4 // Inside height remains 3, so step Y is 4

        val charWidth = stepX * width + 1
        val charHeight = stepY * height + 1
        val renderGrid = Array(charHeight) { CharArray(charWidth) { ' ' } }

        // PASS 1: Draw all walls and corners first so boundaries are established globally
        for (y in 0 until height) {
            for (x in 0 until width) {
                val room = grid[y * width + x] ?: continue

                val cx = stepX * x + (stepX / 2)
                val cy = stepY * (height - 1 - y) + (stepY / 2)

                // Draw corners
                renderGrid[cy - 2][cx - wallOffset] = '+'
                renderGrid[cy - 2][cx + wallOffset] = '+'
                renderGrid[cy + 2][cx - wallOffset] = '+'
                renderGrid[cy + 2][cx + wallOffset] = '+'

                // Draw horizontal walls (North & South)
                for (dx in -halfWidth..halfWidth) {
                    renderGrid[cy - 2][cx + dx] = '-'
                    renderGrid[cy + 2][cx + dx] = '-'
                }

                // Draw vertical walls (West & East)
                for (dy in -1..1) {
                    renderGrid[cy + dy][cx - wallOffset] = '|'
                    renderGrid[cy + dy][cx + wallOffset] = '|'
                }
            }
        }

        // PASS 2: Overwrite the structure with labels, keys, and doors
        for (y in 0 until height) {
            for (x in 0 until width) {
                val room = grid[y * width + x] ?: continue

                val cx = stepX * x + (stepX / 2)
                val cy = stepY * (height - 1 - y) + (stepY / 2)

                // Draw room label (centered on the line above the middle line)
                writeCentered(renderGrid, cy - 1, cx, room.type.name.take(innerRoomWidth - 2))

                // Draw room contents / dropped keys
                if (room.keyIds.isNotEmpty()) {
                    // Place the centre dot on the middle row
                    renderGrid[cy][cx] = '.'
                    // Write the key abbreviation on the lower row
                    val firstKeyStr = getKeyAbbrev(room.keyIds.first()).lowercase()
                    writeCentered(renderGrid, cy + 1, cx, firstKeyStr)
                } else {
                    renderGrid[cy][cx] = '.'
                }

                // Draw doors
                for (dir in Direction.westClockwise) {
                    val door = room.doors[dir.index]

                    // Midpoint offsets for doors
                    val wx = cx + when (dir) {
                        Direction.WEST -> -wallOffset
                        Direction.EAST -> wallOffset
                        else -> 0
                    }
                    val wy = cy + when (dir) {
                        Direction.NORTH -> -2
                        Direction.SOUTH -> 2
                        else -> 0
                    }

                    if (door == null) {
                        continue
                    }

                    // Check if traversing outwards (from parent to child)
                    val adjacentRoom = room.adjacentRooms[dir.index]
                    val isOutward = adjacentRoom != null && adjacentRoom.parent == room

                    // Determine door label based on door type and outward traversal status
                    val doorLabel = when {
                        door is DungeonDoor.Locked -> {
                            getKeyAbbrev(door.key).uppercase()
                        }
                        isOutward && door is DungeonDoor.Guardian -> {
                            "guardian".take(innerRoomWidth - 2).uppercase()
                        }
                        isOutward && door is DungeonDoor.Blocked -> {
                            "${door.level}${door.skill.name}".take(innerRoomWidth - 2).uppercase()
                        }
                        else -> {
                            "   " // 3-space gap for empty doors / open passages
                        }
                    }

                    writeCentered(renderGrid, wy, wx, doorLabel)
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
                    val keysStr = room.keyIds.joinToString(", ") { getKeyAbbrev(it).lowercase() }
                    val typeStr = when (room.type) {
                        DungeonRoomType.Base -> "Start Room"
                        DungeonRoomType.Boss -> "Boss Room"
                        DungeonRoomType.Puzzle -> "Puzzle Room"
                        DungeonRoomType.Normal -> if (room.isCritical) "Critical Normal" else "Bonus Normal"
                    }
                    val zoneStr = room.zone?.let { " [Template at: (${it.x}, ${it.y}), Rot: ${room.rotation * 90}]" } ?: ""
                    println("  Room at ($x, $y) [$typeStr]$zoneStr contains key(s): [ $keysStr ]")
                }
            }
        }
        if (!keysFound) println("  No keys placed.")
    }

    /**
     * Helper to write a string centered horizontally at a specific coordinate on the grid.
     * Prevents empty spaces in door labels from overwriting existing alphanumeric characters.
     */
    private fun writeCentered(grid: Array<CharArray>, y: Int, x: Int, text: String) {
        val startX = x - (text.length / 2)
        for (i in text.indices) {
            val targetX = startX + i
            if (targetX in grid[y].indices) {
                val existingChar = grid[y][targetX]
                val incomingChar = text[i]

                // Guard against overwriting an established door/key name with path whitespace
                if (incomingChar == ' ' && (existingChar.isLetterOrDigit() || existingChar == '_')) {
                    continue
                }
                grid[y][targetX] = incomingChar
            }
        }
    }

    /**
     * Converts a key string ID (e.g. "blue_rectangle_key") into a 5-letter abbreviation.
     * Takes the first letter of the colour and the first letter of the shape.
     * Special handling maps "black" to 'k' to avoid conflicts with "blue".
     */
    private fun getKeyAbbrev(keyId: String): String {
        val parts = keyId.lowercase().split("_")
        val colorWord = parts[0]
        val shapeWord = parts[1]
        val abbrev = "${colorWord.take(3)}_${shapeWord.take(3)}"
        return abbrev
    }
}