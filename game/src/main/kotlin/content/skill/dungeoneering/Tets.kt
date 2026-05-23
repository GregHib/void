package content.skill.dungeoneering
import java.util.ArrayDeque
import kotlin.collections.set
import kotlin.random.Random

enum class Direction(val dx: Int, val dy: Int) {
    NORTH(0, -1), SOUTH(0, 1), EAST(1, 0), WEST(-1, 0);

    fun opposite(): Direction = when (this) {
        NORTH -> SOUTH
        SOUTH -> NORTH
        EAST -> WEST
        WEST -> EAST
    }
}

enum class RoomType {
    START, BOSS, NORMAL, PUZZLE
}

enum class DoorType {
    NORMAL, GUARDIAN, SKILL
}

class Door(
    var isLocked: Boolean = false,
    var keyId: Int? = null,
    var type: DoorType = DoorType.NORMAL
)

class Room(val x: Int, val y: Int, val isCritical: Boolean) {
    var type: RoomType = RoomType.NORMAL
    val keyIds = mutableListOf<Int>()
    val doors = mutableMapOf<Direction, Door>() // TODO replace with array
}

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
                    RoomType.START -> 'S'
                    RoomType.BOSS -> 'B'
                    RoomType.PUZZLE -> 'P'
                    RoomType.NORMAL -> {
                        if (room.keyIds.isNotEmpty()) {
                            val firstKey = room.keyIds.first()
                            getKeyChar(firstKey, totalCritKeys)
                        } else {
                            '.'
                        }
                    }
                }

                // Draw doors or walls
                for (dir in Direction.entries) {
                    val wx = cx + dir.dx
                    val wy = cy + dir.dy
                    val door = room.doors[dir]

                    if (door != null) {
                        if (door.isLocked) {
                            renderGrid[wy][wx] = getLockChar(door.keyId!!, totalCritKeys)
                        } else {
                            // Check if traversing outwards (from parent to child)
                            val adjacentRoom = grid[(y + dir.dy) * width + (x + dir.dx)]!!
                            val isOutward = parentMap[adjacentRoom] == room

                            if (isOutward && door.type == DoorType.GUARDIAN) {
                                renderGrid[wy][wx] = 'G'
                            } else if (isOutward && door.type == DoorType.SKILL) {
                                renderGrid[wy][wx] = 'K'
                            } else {
                                renderGrid[wy][wx] = ' ' // Standard open passage
                            }
                        }
                    } else {
                        renderGrid[wy][wx] = if (dir == Direction.NORTH || dir == Direction.SOUTH) '-' else '|'
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
                        RoomType.START -> "Start Room"
                        RoomType.BOSS -> "Boss Room"
                        RoomType.PUZZLE -> "Puzzle Room"
                        RoomType.NORMAL -> if (room.isCritical) "Critical Normal" else "Bonus Normal"
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
        canTraverse: (from: Room, door: Door, neighbour: Room) -> Boolean = { _, _, _ -> true }
    ): List<Room> {
        val visitedList = mutableListOf<Room>()
        val visitedSet = mutableSetOf<Room>()
        val queue = ArrayDeque<Room>()

        queue.add(start)
        visitedSet.add(start)

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            visitedList.add(curr)
            for ((dir, door) in curr.doors) {
                val nx = curr.x + dir.dx
                val ny = curr.y + dir.dy
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
        for (coord in pathCoords) {
            val room = Room(coord.first, coord.second, isCritical = true)
            grid[coord.second * width + coord.first] = room
            pathRooms.add(room)
        }

        pathRooms.first().type = RoomType.START
        pathRooms.last().type = RoomType.BOSS

        // 2. Select critical doors to lock
        val criticalDoors = mutableListOf<Pair<Room, Room>>()
        for (i in 0 until pathRooms.size - 1) {
            criticalDoors.add(Pair(pathRooms[i], pathRooms[i + 1]))
        }

        val bossDoorEdge = criticalDoors.last()
        val lockedCriticalEdges = mutableListOf<Pair<Room, Room>>()

        if (adjCritKeys >= 1) {
            val shouldLockBossDoor = random.nextDouble() < bossDoorLockChance
            if (shouldLockBossDoor) {
                lockedCriticalEdges.add(bossDoorEdge)
                val availableCount = criticalDoors.size - 1
                val neededCount = adjCritKeys - 1
                if (availableCount > 0 && neededCount > 0) {
                    val indices = IntArray(availableCount) { it }
                    for (i in availableCount - 1 downTo 1) {
                        val j = random.nextInt(i + 1)
                        val temp = indices[i]
                        indices[i] = indices[j]
                        indices[j] = temp
                    }
                    for (i in 0 until neededCount) {
                        lockedCriticalEdges.add(criticalDoors[indices[i]])
                    }
                }
            } else {
                val availableCount = criticalDoors.size
                val indices = IntArray(availableCount) { it }
                for (i in availableCount - 1 downTo 1) {
                    val j = random.nextInt(i + 1)
                    val temp = indices[i]
                    indices[i] = indices[j]
                    indices[j] = temp
                }
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
            val door = Door(isLocked = true, keyId = idx)
            r1.doors[dir] = door
            r2.doors[dir.opposite()] = door
        }

        // Connect remaining unlocked critical passages
        for (edge in criticalDoors) {
            if (edge !in sortedLockedCriticalEdges) {
                val r1 = edge.first
                val r2 = edge.second
                val dir = getDirection(r1, r2)
                val door = Door(isLocked = false)
                r1.doors[dir] = door
                r2.doors[dir.opposite()] = door
            }
        }

        // 3. Grow bonus rooms
        val bonusRooms = mutableListOf<Room>()
        var currentRoomsCount = pathRooms.size
        val targetBonusRooms = adjTotalRooms - adjCritRooms

        for (b in 0 until targetBonusRooms) {
            val candidates = grid.filter { it != null && it.type != RoomType.BOSS }
            val attachment = findAttachmentPoint(grid, candidates) ?: break

            val (parentRoom, dir) = attachment
            val nx = parentRoom.x + dir.dx
            val ny = parentRoom.y + dir.dy

            val newRoom = Room(nx, ny, isCritical = false)
            grid[ny * width + nx] = newRoom
            bonusRooms.add(newRoom)

            val door = Door(isLocked = false)
            parentRoom.doors[dir] = door
            newRoom.doors[dir.opposite()] = door
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
            val door = parent.doors[dir]!!
            door.isLocked = true
            door.keyId = adjCritKeys + idx
        }

        // 6. Assign room properties (Puzzle rooms) and door types
        val allActiveRooms = grid.filterNotNull()
        for (room in allActiveRooms) {
            if (room.type == RoomType.NORMAL && room != startRoom && room != pathRooms.last()) {
                // Ensure puzzle rooms are not leaf nodes (they must lead somewhere)
                val hasChildren = allActiveRooms.any { parentMap[it] == room }
                if (hasChildren && random.nextDouble() < puzzleRoomChance) {
                    room.type = RoomType.PUZZLE
                }
            }

            for ((dir, door) in room.doors) {
                if (!door.isLocked) {
                    val neighbor = grid[(room.y + dir.dy) * width + (room.x + dir.dx)]!!
                    if (parentMap[neighbor] == room) { // Outward-facing transition
                        val r = random.nextDouble()
                        door.type = when {
                            r < skillDoorChance -> DoorType.SKILL
                            r < skillDoorChance + guardianDoorChance -> DoorType.GUARDIAN
                            else -> DoorType.NORMAL
                        }
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
                !door.isLocked || (door.keyId != null && door.keyId!! < i)
            }

            val candidates = if (isCriticalKey) {
                reachable.filter { it.isCritical && it.type != RoomType.BOSS }
            } else {
                reachable.filter { it.type != RoomType.BOSS }
            }

            val emptyCandidates = candidates.filter { it.keyIds.isEmpty() }
            val targetRoom = if (emptyCandidates.isNotEmpty()) {
                emptyCandidates.shuffled(random).first()
            } else {
                candidates.minByOrNull { it.keyIds.size }!!
            }

            targetRoom.keyIds.add(i)
        }

        return DungeonMap(width, height, grid, parentMap)
    }

    private fun findPathOnGrid(targetLength: Int): List<Pair<Int, Int>> {

        val totalCells = width * height
        val indices = IntArray(totalCells) { it }

        // In-place Fisher-Yates shuffle
        for (i in totalCells - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = indices[i]
            indices[i] = indices[j]
            indices[j] = temp
        }
        for (idx in indices) {
            val x = idx % width
            val y = idx / width
            val path = findPath(x, y, targetLength)
            if (path != null) return path
        }
        throw IllegalStateException("A path of length $targetLength cannot fit on a ${width}x${height} grid.")
    }

    val localDirs = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
    private fun findPath(startX: Int, startY: Int, length: Int): List<Pair<Int, Int>>? {
        val path = mutableListOf<Pair<Int, Int>>()
        val visited = BooleanArray(width * height)
        fun dfs(cx: Int, cy: Int): Boolean {
            path.add(Pair(cx, cy))
            visited[cy * width + cx] = true
            if (path.size == length) return true

            for (i in 3 downTo 1) {
                val j = random.nextInt(i + 1)
                val temp = localDirs[i]
                localDirs[i] = localDirs[j]
                localDirs[j] = temp
            }
            for (dir in localDirs) {
                val nx = cx + dir.dx
                val ny = cy + dir.dy
                if (nx in 0 until width && ny in 0 until height && !visited[ny * width + nx]) {
                    if (dfs(nx, ny)) return true
                }
            }

            path.removeAt(path.lastIndex)
            visited[cy * width + cx] = false
            return false
        }

        if (dfs(startX, startY)) {
            return path
        }
        return null
    }

    private fun findAttachmentPoint(grid: Array<Room?>, rooms: Collection<Room?>): Pair<Room, Direction>? {
        for (room in rooms.shuffled(random)) {
            for (dir in Direction.entries.shuffled(random)) {
                val nx = room!!.x + dir.dx
                val ny = room.y + dir.dy
                if (nx in 0 until width && ny in 0 until height && grid[ny * width + nx] == null) {
                    return Pair(room, dir)
                }
            }
        }
        return null
    }

    private fun getDirection(from: Room, to: Room): Direction {
        val dx = to.x - from.x
        val dy = to.y - from.y
        return Direction.entries.first { it.dx == dx && it.dy == dy }
    }
}

fun main() {
    val width = 5
    val height = 5
    val totalRooms = 12
    val criticalRooms = 6
    val criticalKeys = 3
    val bonusKeys = 2

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
    generator.generate()
    val start = System.currentTimeMillis()
    repeat(10) {

        generator.generate()
    }
    println("Took ${System.currentTimeMillis() - start}ms")

//    dungeon.prettyPrint(totalCritKeys = criticalKeys)

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
