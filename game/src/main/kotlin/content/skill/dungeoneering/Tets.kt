package content.skill.dungeoneering
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import java.util.ArrayDeque
import kotlin.collections.lastIndex
import kotlin.collections.set
import kotlin.random.Random

enum class RoomType {
    Base, Boss, Normal, Puzzle
}

sealed class Door {
    object Normal : Door()
    object Guardian : Door()
    object Skill : Door()
    data class Locked(val key: Int) : Door()
}

class Room(val tile: Tile, val isCritical: Boolean) {
    var type: RoomType = RoomType.Normal
    val keyIds = mutableListOf<Int>()
    val doors = arrayOfNulls<Door>(4)
}

private val Direction.index: Int
    get() = ordinal / 2

class DungeonMap(
    val width: Int,
    val height: Int,
    val grid: Array<Room?>,
    private val parentMap: Map<Room, Room>
) {

    fun prettyPrint(totalCritKeys: Int) {
        val charWidth = 2 * width + 1
        val charHeight = 2 * height + 1
        val renderGrid = Array(charHeight) { CharArray(charWidth) { ' ' } }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val room = grid[y * width + x] ?: continue
                val cx = 2 * x + 1
                val cy = 2 * y + 1

                // Draw corners
                renderGrid[cy - 1][cx - 1] = '+'
                renderGrid[cy - 1][cx + 1] = '+'
                renderGrid[cy + 1][cx - 1] = '+'
                renderGrid[cy + 1][cx + 1] = '+'

                // Draw room center
                renderGrid[cy][cx] = when (room.type) {
                    RoomType.Base -> 'S'
                    RoomType.Boss -> 'B'
                    RoomType.Puzzle -> 'P'
                    RoomType.Normal -> {
                        if (room.keyIds.isNotEmpty()) {
                            val firstKey = room.keyIds.first()
                            getKeyChar(firstKey, totalCritKeys)
                        } else {
                            '.'
                        }
                    }
                }

                // Draw doors or walls
                for (dir in Direction.cardinal) {
                    val wx = cx + dir.delta.x
                    val wy = cy + dir.delta.y
                    val door = room.doors[dir.index]

                    if (door == null) {
                        renderGrid[wy][wx] = if (dir == Direction.NORTH || dir == Direction.SOUTH) '-' else '|'
                        continue
                    }

                    if (door is Door.Locked) {
                        renderGrid[wy][wx] = getLockChar(door.key, totalCritKeys)
                        continue
                    }

                    // Check if traversing outwards (from parent to child)
                    val adjacentRoom = grid[(y + dir.delta.y) * width + (x + dir.delta.x)]!!
                    val isOutward = parentMap[adjacentRoom] == room

                    if (isOutward && door is Door.Guardian) {
                        renderGrid[wy][wx] = 'G'
                    } else if (isOutward && door is Door.Skill) {
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
        for (y in 0 until height) {
            for (x in 0 until width) {
                val room = grid[y * width + x] ?: continue
                if (room.keyIds.isNotEmpty()) {
                    keysFound = true
                    val keysStr = room.keyIds.map { getKeyChar(it, totalCritKeys) }.joinToString(", ")
                    val typeStr = when (room.type) {
                        RoomType.Base -> "Start Room"
                        RoomType.Boss -> "Boss Room"
                        RoomType.Puzzle -> "Puzzle Room"
                        RoomType.Normal -> if (room.isCritical) "Critical Normal" else "Bonus Normal"
                    }
                    println("  Room at ($x, $y) [$typeStr] contains key(s): [ $keysStr ]")
                }
            }
        }
        if (!keysFound) println("  No keys placed.")
    }

    private fun getKeyChar(keyId: Int, totalCritKeys: Int): Char {
        return if (keyId < totalCritKeys) {
            ('a'.code + keyId).toChar() // Critical key (a, b, c...)
        } else {
            ('u'.code + (keyId - totalCritKeys)).toChar() // Bonus key (u, v, w...)
        }
    }

    private fun getLockChar(keyId: Int, totalCritKeys: Int): Char {
        return if (keyId < totalCritKeys) {
            ('A'.code + keyId).toChar() // Critical lock (A, B, C...)
        } else {
            ('U'.code + (keyId - totalCritKeys)).toChar() // Bonus lock (U, V, W...)
        }
    }
}

class DungeonGenerator(
    val width: Int,
    val height: Int,
    val totalRooms: Int,
    val criticalRoomsCount: Int,
    val criticalKeysCount: Int,
    val bonusKeysCount: Int,
    val puzzleRoomChance: Double = 0.20,
    val skillDoorChance: Double = 0.15,
    val guardianDoorChance: Double = 0.15,
    val bossDoorLockChance: Double = 0.90,
    val random: Random = Random.Default
) {

    private fun Tile.index() = y * width + x
    private var adjTotalRooms = totalRooms
    private var adjCritRooms = criticalRoomsCount
    private var adjCritKeys = criticalKeysCount
    private var adjBonusKeys = bonusKeysCount

    init {
        validateAndAdjustParameters()
    }

    private fun validateAndAdjustParameters() {
        val maxRooms = width * height
        if (adjTotalRooms > maxRooms) adjTotalRooms = maxRooms
        if (adjTotalRooms < 3) adjTotalRooms = 3

        if (adjCritRooms < 2) adjCritRooms = 2
        if (adjCritRooms > adjTotalRooms) adjCritRooms = adjTotalRooms

        if (adjCritKeys > adjCritRooms - 1) adjCritKeys = adjCritRooms - 1
        if (adjCritKeys < 0) adjCritKeys = 0

        val bonusRoomsCount = adjTotalRooms - adjCritRooms
        if (adjBonusKeys > bonusRoomsCount) adjBonusKeys = bonusRoomsCount
        if (adjBonusKeys < 0) adjBonusKeys = 0
    }

    private fun bfs(
        start: Room,
        grid: Array<Room?>,
        canTraverse: (from: Room, door: Door?, neighbour: Room) -> Boolean = { _, _, _ -> true }
    ): List<Room> {
        val visitedList = mutableListOf<Room>()
        val visitedSet = mutableSetOf<Room>()
        val queue = ArrayDeque<Room>()

        queue.add(start)
        visitedSet.add(start)

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            visitedList.add(curr)
            for ((i, door) in curr.doors.withIndex()) {
                val dir = Direction.cardinal[i]
                val nx = curr.tile.x + dir.delta.x
                val ny = curr.tile.y + dir.delta.y
                if (nx in 0 until width && ny in 0 until height) {
                    val neighbour = grid[ny * width + nx]
                    if (neighbour != null && neighbour !in visitedSet) {
                        if (canTraverse(curr, door, neighbour)) {
                            visitedSet.add(neighbour)
                            queue.add(neighbour)
                        }
                    }
                }
            }
        }
        return visitedList
    }

    fun generate(): DungeonMap {
        val grid = arrayOfNulls<Room>(width * height)

        // 1. Generate the critical path
        val pathCoords = findPathOnGrid(adjCritRooms)
        val pathRooms = mutableListOf<Room>()
        for (tile in pathCoords) {
            val room = Room(tile, isCritical = true)
            grid[tile.index()] = room
            pathRooms.add(room)
        }

        pathRooms.first().type = RoomType.Base
        pathRooms.last().type = RoomType.Boss

        // 2. Select critical doors to lock
        val criticalDoors = mutableListOf<Pair<Room, Room>>()
        for (i in 0 until pathRooms.lastIndex) {
            criticalDoors.add(Pair(pathRooms[i], pathRooms[i + 1]))
        }

        val bossDoorEdge = criticalDoors.last()
        val lockedCriticalEdges = mutableListOf<Pair<Room, Room>>()

        if (adjCritKeys >= 1) {
            val shouldLockBossDoor = random.nextDouble() < bossDoorLockChance
            if (shouldLockBossDoor) {
                lockedCriticalEdges.add(bossDoorEdge)
                val availableCount = criticalDoors.lastIndex
                val neededCount = adjCritKeys - 1
                if (availableCount > 0 && neededCount > 0) {
                    val indices = IntArray(availableCount) { it }
                    indices.shuffleInPlace()
                    for (i in 0 until neededCount) {
                        lockedCriticalEdges.add(criticalDoors[indices[i]])
                    }
                }
            } else {
                val availableCount = criticalDoors.size
                val indices = IntArray(availableCount) { it }
                indices.shuffleInPlace()
                for (i in 0 until adjCritKeys) {
                    lockedCriticalEdges.add(criticalDoors[indices[i]])
                }
            }
        }

        val sortedLockedCriticalEdges = lockedCriticalEdges.sortedBy { pathRooms.indexOf(it.first) }
        for ((idx, edge) in sortedLockedCriticalEdges.withIndex()) {
            val r1 = edge.first
            val r2 = edge.second
            val dir = getDirection(r1, r2)
            val door = Door.Locked(idx)
            r1.doors[dir.index] = door
            r2.doors[dir.inverse().index] = door
        }

        // Connect remaining unlocked critical passages
        for (edge in criticalDoors) {
            if (edge in sortedLockedCriticalEdges) {
                continue
            }
            val r1 = edge.first
            val r2 = edge.second
            val dir = getDirection(r1, r2)
            val door = Door.Normal
            r1.doors[dir.index] = door
            r2.doors[dir.inverse().index] = door
        }

        // 3. Grow bonus rooms
        val bonusRooms = mutableListOf<Room>()
        var currentRoomsCount = pathRooms.size
        val targetBonusRooms = adjTotalRooms - adjCritRooms

        for (b in 0 until targetBonusRooms) {
            val candidates = grid.filter { it != null && it.type != RoomType.Boss }
            val attachment = findAttachmentPoint(grid, candidates) ?: break

            val (parentRoom, dir) = attachment
            val tile = parentRoom.tile.add(dir)

            val newRoom = Room(tile, isCritical = false)
            grid[tile.index()] = newRoom
            bonusRooms.add(newRoom)

            val door = Door.Normal
            parentRoom.doors[dir.index] = door
            newRoom.doors[dir.inverse().index] = door
            currentRoomsCount++
        }

        // 4. Use BFS to assign parent-child hierarchy
        val startRoom = pathRooms.first()
        val parentMap = mutableMapOf<Room, Room>()
        bfs(startRoom, grid) { curr, _, neighbour ->
            parentMap[neighbour] = curr
            true
        }

        // 5. Select and configure locked doors on the bonus path
        val bonusDoors = mutableListOf<Pair<Room, Room>>()
        for (room in bonusRooms) {
            val parent = parentMap[room]!!
            bonusDoors.add(Pair(parent, room))
        }
        val lockedBonusEdges = bonusDoors.shuffled(random).take(adjBonusKeys)
        for ((idx, edge) in lockedBonusEdges.withIndex()) {
            val parent = edge.first
            val child = edge.second
            val dir = getDirection(parent, child)
            parent.doors[dir.index] = Door.Locked(adjCritKeys + idx)
        }

        // 6. Assign room properties (Puzzle rooms) and door types
        val allActiveRooms = grid.filterNotNull()
        for (room in allActiveRooms) {
            if (room.type == RoomType.Normal && room != startRoom && room != pathRooms.last()) {
                // Ensure puzzle rooms are not leaf nodes (they must lead somewhere)
                val hasChildren = allActiveRooms.any { parentMap[it] == room }
                if (hasChildren && random.nextDouble() < puzzleRoomChance) {
                    room.type = RoomType.Puzzle
                }
            }

            for ((i, door) in room.doors.withIndex()) {
                if (door == null) {
                    continue
                }
                val dir = Direction.cardinal[i]
                if (door is Door.Locked) {
                    continue
                }
                val tile = room.tile.add(dir)
                val neighbor = grid[tile.index()]
                if (neighbor != null && parentMap[neighbor] == room) { // Outward-facing transition
                    val r = random.nextDouble()
                    room.doors[i] = when {
                        r < skillDoorChance -> Door.Skill
                        r < skillDoorChance + guardianDoorChance -> Door.Guardian
                        else -> Door.Normal
                    }
                }
            }
        }

        // 7. Place keys preserving strict solvability
        val totalKeys = adjCritKeys + adjBonusKeys
        for (i in 0 until totalKeys) {
            val isCriticalKey = (i < adjCritKeys)
            // Unified conditional BFS search
            val reachable = bfs(startRoom, grid) { _, door, _ ->
                door !is Door.Locked || (door.key < i)
            }
            val candidates = reachable.filter { it.isCritical == isCriticalKey && it.type != RoomType.Boss }
            val emptyCandidates = candidates.filter { it.keyIds.isEmpty() }
            val targetRoom = if (emptyCandidates.isNotEmpty()) {
                emptyCandidates.random(random)
            } else {
                candidates.minBy { it.keyIds.size }
            }
            targetRoom.keyIds.add(i)
        }

        return DungeonMap(width, height, grid, parentMap)
    }

    private fun findPathOnGrid(targetLength: Int): List<Tile> {
        val indices = IntArray(width * height) { it }
        indices.shuffleInPlace()
        for (idx in indices) {
            val x = idx % width
            val y = idx / width
            val path = findPath(x, y, targetLength)
            if (path != null) {
                return path
            }
        }
        throw IllegalStateException("A path of length $targetLength cannot fit on a ${width}x${height} grid.")
    }

    private fun IntArray.shuffleInPlace() {
        for (i in lastIndex downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = this[i]
            this[i] = this[j]
            this[j] = temp
        }
    }

    private fun <T> Array<T>.shuffleInPlace() {
        for (i in lastIndex downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = this[i]
            this[i] = this[j]
            this[j] = temp
        }
    }
    private fun findPath(startX: Int, startY: Int, length: Int): List<Tile>? {
        val path = mutableListOf<Tile>()
        val visited = BooleanArray(width * height)
        val localDirs = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)

        fun dfs(tile: Tile): Boolean {
            path.add(tile)
            visited[tile.index()] = true
            if (path.size == length) {
                return true
            }

            localDirs.shuffleInPlace()
            for (dir in localDirs) {
                val neighbour = tile.add(dir)
                if (neighbour.x in 0 until width && neighbour.y in 0 until height && !visited[neighbour.index()]) {
                    if (dfs(neighbour)) {
                        return true
                    }
                }
            }

            path.removeAt(path.lastIndex)
            visited[tile.index()] = false
            return false
        }

        if (dfs(Tile(startX, startY))) {
            return path
        }
        return null
    }

    private fun findAttachmentPoint(grid: Array<Room?>, rooms: Collection<Room?>): Pair<Room, Direction>? {
        for (room in rooms.shuffled(random)) {
            if (room == null) {
                continue
            }
            for (dir in Direction.cardinal.shuffled(random)) {
                val tile = room.tile.add(dir)
                if (tile.x in 0 until width && tile.y in 0 until height && grid[tile.index()] == null) {
                    return Pair(room, dir)
                }
            }
        }
        return null
    }

    private fun getDirection(from: Room, to: Room): Direction {
        return to.tile.delta(from.tile).toDirection()
    }
}

fun main() {
    val width = 4
    val height = 4
    val totalRooms = 6
    val criticalRooms = 6
    val criticalKeys = 2
    val bonusKeys = 1

    println("Generating structured dungeon...")

    val generator = DungeonGenerator(
        width = width,
        height = height,
        totalRooms = totalRooms,
        criticalRoomsCount = criticalRooms,
        criticalKeysCount = criticalKeys,
        bonusKeysCount = bonusKeys,
        puzzleRoomChance = 0.35,
        skillDoorChance = 0.20,
        guardianDoorChance = 0.20,
        bossDoorLockChance = 0.95
    )
    val start = System.currentTimeMillis()
    val dungeon = generator.generate()
    println("Took ${System.currentTimeMillis() - start}ms")

    dungeon.prettyPrint(totalCritKeys = criticalKeys)

    println("\nLegend:")
    println("  S   : Start Room")
    println("  B   : Boss Room (Goal)")
    println("  P   : Puzzle Room (Guaranteed to have an exit)")
    println("  .   : Normal Room")
    println("  a-t : Critical Keys (lowercase)")
    println("  u-z : Bonus Keys (lowercase)")
    println("  A-T : Critical Locked Doors (uppercase)")
    println("  U-Z : Bonus Locked Doors (uppercase)")
    println("  G   : Outward Guardian Door")
    println("  K   : Outward Skill Door")
    println("  + - | : Walls and normal corridors")
}
