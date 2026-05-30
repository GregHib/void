package content.skill.dungeoneering

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.random
import kotlin.collections.lastIndex
import kotlin.random.nextInt

class DungeonGenerator(
    size: DungeonSize,
    val floor: Int,
    val complexity: Int,
    // Estimated chances
    private val puzzleRoomChance: Double = 0.33,
    private val skillDoorChance: Double = 0.33,
    private val guardianDoorChance: Double = 0.33,
    private val bossDoorLockChance: Double = 0.90,
) {
    val width: Int = when (size) {
        DungeonSize.Small -> 5
        DungeonSize.Medium -> 4
        DungeonSize.Large -> 8
    }
    val height: Int = when (size) {
        DungeonSize.Small -> 4
        DungeonSize.Medium -> 8
        DungeonSize.Large -> 8
    }
    private val totalRooms = when (size) {
        DungeonSize.Small -> random.nextInt(10..16)
        DungeonSize.Medium -> random.nextInt(23..32)
        DungeonSize.Large -> random.nextInt(50..64)
    }
    private val criticalRoomsCount = when (size) {
        DungeonSize.Small -> random.nextInt(6..8)
        DungeonSize.Medium -> random.nextInt(10..14)
        DungeonSize.Large -> random.nextInt(19..23)
    }
    private val criticalKeysCount = when (size) {
        DungeonSize.Small -> random.nextInt(2..3)
        DungeonSize.Medium -> random.nextInt(3..5)
        DungeonSize.Large -> random.nextInt(5..8)
    }
    private val bonusKeysCount = when (size) { // These are not known
        DungeonSize.Small -> random.nextInt(2..3)
        DungeonSize.Medium -> random.nextInt(3..5)
        DungeonSize.Large -> random.nextInt(5..8)
    }
    private val keys = Tables.itemList("dungeon_keys.all.keys").shuffled(random)

    fun generate(maxSkills: Map<Skill, Int> = emptyMap()): DungeonMap {
        val grid = arrayOfNulls<DungeonRoom>(width * height)
        val path = createCriticalPath(grid)
        lockCriticalDoors(path)
        val bonusRooms = createBonusRooms(path, grid)
        // Link rooms together
        val startRoom = path.first()
        DungeonMap.traverse(startRoom, width, height, grid) { curr, _, neighbour ->
            neighbour.parent = curr
            true
        }
        lockBonusDoors(bonusRooms)
        assignRoomTypes(grid, maxSkills)
        placeKeys(startRoom, grid)
        val themeName = theme()
        populateMap(grid, themeName)
        return DungeonMap(width, height, startRoom.tile, grid, themeName)
    }

    /**
     * Find a valid path of length [criticalRoomsCount] and set start base and end boss rooms.
     */
    internal fun createCriticalPath(grid: Array<DungeonRoom?>): MutableList<DungeonRoom> {
        val pathRooms = mutableListOf<DungeonRoom>()
        val pathCoords = findPathOnGrid(criticalRoomsCount)
        for (tile in pathCoords) {
            val room = DungeonRoom(tile, isCritical = true)
            grid[tile.index()] = room
            pathRooms.add(room)
        }
        pathRooms.first().type = DungeonRoomType.Base
        pathRooms.last().type = DungeonRoomType.Boss
        return pathRooms
    }

    internal fun findPathOnGrid(targetLength: Int): List<Tile> {
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
        throw IllegalStateException("A path of length $targetLength cannot fit on a ${width}x$height grid.")
    }

    internal fun findPath(startX: Int, startY: Int, length: Int): List<Tile>? {
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

    /**
     * Randomly assign locks to doors along [path] ensuring they are in depth order
     */
    internal fun lockCriticalDoors(path: MutableList<DungeonRoom>) {
        val criticalDoors = mutableListOf<Pair<DungeonRoom, DungeonRoom>>()
        for (i in 0 until path.lastIndex) {
            criticalDoors.add(Pair(path[i], path[i + 1]))
        }
        val bossDoorEdge = criticalDoors.last()
        val lockedCriticalEdges = mutableListOf<Pair<DungeonRoom, DungeonRoom>>()

        if (criticalKeysCount >= 1) {
            val shouldLockBossDoor = random.nextDouble() < bossDoorLockChance
            if (shouldLockBossDoor) {
                lockedCriticalEdges.add(bossDoorEdge)
                val availableCount = criticalDoors.lastIndex
                val neededCount = criticalKeysCount - 1
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
                for (i in 0 until criticalKeysCount) {
                    lockedCriticalEdges.add(criticalDoors[indices[i]])
                }
            }
        }

        val sortedLockedCriticalEdges = lockedCriticalEdges.sortedBy { path.indexOf(it.first) }
        for ((idx, edge) in sortedLockedCriticalEdges.withIndex()) {
            val r1 = edge.first
            val r2 = edge.second
            val dir = getDirection(r1, r2)
            connect(r1, r2, dir, DungeonDoor.Locked(keys[idx], idx), DungeonDoor.Normal)
        }

        // Connect remaining unlocked critical passages
        for (edge in criticalDoors) {
            if (edge in sortedLockedCriticalEdges) {
                continue
            }
            val r1 = edge.first
            val r2 = edge.second
            val dir = getDirection(r1, r2)
            connect(r1, r2, dir, DungeonDoor.Normal)
        }
    }

    /**
     *  Add bonus rooms randomly to empty doors along the [criticalPath]
     */
    internal fun createBonusRooms(criticalPath: MutableList<DungeonRoom>, grid: Array<DungeonRoom?>): MutableList<DungeonRoom> {
        val bonusRooms = mutableListOf<DungeonRoom>()
        var roomCount = criticalPath.size
        val bonusRoomCount = totalRooms - criticalRoomsCount
        for (b in 0 until bonusRoomCount) {
            val candidates = grid.filter { it != null && it.type != DungeonRoomType.Boss }
            val attachment = findAttachmentPoint(grid, candidates) ?: break

            val (parentRoom, dir) = attachment
            val tile = parentRoom.tile.add(dir)

            val newRoom = DungeonRoom(tile, isCritical = false)
            grid[tile.index()] = newRoom
            bonusRooms.add(newRoom)

            connect(parentRoom, newRoom, dir, DungeonDoor.Normal)
            roomCount++
        }
        return bonusRooms
    }

    internal fun findAttachmentPoint(grid: Array<DungeonRoom?>, rooms: Collection<DungeonRoom?>): Pair<DungeonRoom, Direction>? {
        val directions = Direction.westClockwise.toList().toTypedArray()
        for (room in rooms.shuffled(random)) {
            if (room == null) {
                continue
            }
            directions.shuffleInPlace()
            for (dir in directions) {
                val tile = room.tile.add(dir)
                if (tile.x in 0 until width && tile.y in 0 until height && grid[tile.index()] == null) {
                    return Pair(room, dir)
                }
            }
        }
        return null
    }

    /**
     * Randomly lock doors on bonus path rooms
     */
    internal fun lockBonusDoors(bonusRooms: MutableList<DungeonRoom>) {
        val bonusDoors = mutableListOf<Pair<DungeonRoom, DungeonRoom>>()
        for (room in bonusRooms) {
            val parent = room.parent ?: continue
            bonusDoors.add(Pair(parent, room))
        }
        val lockedBonusEdges = bonusDoors.shuffled(random).take(bonusKeysCount)
        for ((idx, edge) in lockedBonusEdges.withIndex()) {
            val parent = edge.first
            val child = edge.second
            val dir = getDirection(parent, child)
            val index = criticalKeysCount + idx
            parent.doors[dir.roomIndex] = DungeonDoor.Locked(keys[index], index)
            child.doors[dir.inverse().roomIndex] = DungeonDoor.Normal
        }
    }

    /**
     * Randomly assign puzzle room, skill, and guardian door types
     */
    internal fun assignRoomTypes(grid: Array<DungeonRoom?>, maxLevels: Map<Skill, Int>) {
        val allActiveRooms = grid.filterNotNull()
        for (room in allActiveRooms) {
            if (room.type == DungeonRoomType.Normal) {
                // Ensure puzzle rooms are not leaf nodes (they must lead somewhere)
                val hasChildren = allActiveRooms.any { it.parent == room }
                if (hasChildren && (complexity == 5 || complexity == 6) && random.nextDouble() < puzzleRoomChance) {
                    room.type = DungeonRoomType.Puzzle
                }
            }

            for ((i, door) in room.doors.withIndex()) {
                if (door == null) {
                    continue
                }
                val dir = Direction.westClockwise[i]
                if (door is DungeonDoor.Locked) {
                    continue
                }
                val tile = room.tile.add(dir)
                val neighbor = grid[tile.index()]
                if (neighbor == null || neighbor.parent != room) {
                    continue // Can't be inward facing
                }
                val r = random.nextDouble()
                val newDoor = when {
                    r < skillDoorChance -> {
                        val skill = DungeonDoor.Blocked.skills.random(random)
                        val level = maxLevels[skill] ?: 1
                        val req = random.nextInt((level - 9).coerceAtLeast(1)..(level + 9).coerceAtMost(if (room.isCritical) level else 110))
                        DungeonDoor.Blocked(skill, req)
                    }
                    r < skillDoorChance + guardianDoorChance -> DungeonDoor.Guardian
                    else -> DungeonDoor.Normal
                }
                room.doors[i] = newDoor
                neighbor.doors[dir.inverse().roomIndex] = DungeonDoor.Normal
            }
        }
    }

    /**
     * Place keys around the map in a way that can be guaranteed to be solvable.
     */
    internal fun placeKeys(startRoom: DungeonRoom, grid: Array<DungeonRoom?>) {
        val totalKeys = criticalKeysCount + bonusKeysCount
        for (depth in 0 until totalKeys) {
            val reachable = DungeonMap.traverse(startRoom, width, height, grid) { _, door, _ ->
                door !is DungeonDoor.Locked || (door.depth < depth) // Ensure room is reachable given keys so far
            }
            val isCriticalKey = depth < criticalKeysCount

            var candidates = reachable.filter { it.isCritical == isCriticalKey && it.type != DungeonRoomType.Boss }
            if (candidates.isEmpty()) {
                candidates = reachable.filter { it.type != DungeonRoomType.Boss }
            }
            val emptyCandidates = candidates.filter { it.keys.isEmpty() }
            val targetRoom = if (emptyCandidates.isNotEmpty()) {
                emptyCandidates.random(random)
            } else {
                candidates.minByOrNull { it.keys.size } ?: error("No valid rooms left for key depth $depth $reachable")
            }
            targetRoom.keys.add(keys[depth])
        }
    }

    /**
     * Populate dungeons rooms with valid map zones
     */
    private fun populateMap(grid: Array<DungeonRoom?>, theme: String) {
        val allActiveRooms = grid.filterNotNull()
        for (room in allActiveRooms) {
            val typeName = room.type.name.lowercase()
            val requiredDoors = BooleanArray(4) { idx ->
                val dir = Direction.westClockwise[idx]
                room.doors[dir.roomIndex] != null
            }
            val matchingOptions = mutableListOf<Pair<Zone, Int>>()
            for (c in complexity downTo 1) { // TODO applies to only normal or all??
                val table = Tables.getOrNull("${theme}_c${c}_$typeName") ?: continue
                for (row in table.rows()) {
                    if (floor in 12..17) {
                        // TODO skip seeker sentinel puzzle, balak pummeller and shadow forgr ihlakhizan bosses
                    }
                    val zone = Zone(row.int("x"), row.int("y"))
                    val actualDoors = row.boolList("doors")
                    if (room.type == DungeonRoomType.Puzzle) {
                        // Puzzles rooms are only valid if the template's original south door
                        // points towards the parent room after rotation is applied.
                        val parent = room.parent
                        if (parent != null) {
                            val entryDir = getDirection(room, parent)
                            // Rotate so puzzle entry door (south) is in the correct orientation
                            val requiredRotation = (entryDir.roomIndex - 3 + 4) % 4
                            if (matchesDoors(actualDoors, requiredDoors, requiredRotation)) {
                                matchingOptions.add(Pair(zone, requiredRotation))
                            }
                        }
                    } else {
                        for (r in 0..3) {
                            if (matchesDoors(actualDoors, requiredDoors, r)) {
                                matchingOptions.add(Pair(zone, r))
                            }
                        }
                    }
                }
            }
            if (matchingOptions.isNotEmpty()) {
                val selection = matchingOptions.random(random)
                room.zone = selection.first
                room.rotation = selection.second
            } else {
                System.err.println("Warning: No matching layout template found for ${theme}_c${complexity}_$typeName room at (${room.tile.x}, ${room.tile.y}) [${requiredDoors.joinToString()}]")
            }
        }
    }

    private fun theme(): String = when (floor) {
        in 1..11 -> "frozen"
        in 12..17, in 30..35 -> "abandoned"
        in 18..29 -> "furnished"
        in 36..47 -> "occult"
        in 48..60 -> "warped"
        else -> ""
    }

    /**
     * Checks if a template door array [W, N, E, S] matches the layout's required doors
     * when rotated clockwise by [rotation] increments of 90 degrees.
     */
    internal fun matchesDoors(template: List<Boolean>, required: BooleanArray, rotation: Int): Boolean {
        for (targetIdx in 0..3) {
            val sourceIdx = (targetIdx + rotation) % 4
            if (template[sourceIdx] != required[targetIdx]) {
                return false
            }
        }
        return true
    }

    private fun getDirection(from: DungeonRoom, to: DungeonRoom): Direction = to.tile.delta(from.tile).toDirection()

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

    private fun Tile.index() = y * width + x

    private fun connect(r1: DungeonRoom, r2: DungeonRoom, dir: Direction, door1: DungeonDoor = DungeonDoor.Normal, door2: DungeonDoor = DungeonDoor.Normal) {
        r1.doors[dir.roomIndex] = door1
        r1.adjacentRooms[dir.roomIndex] = r2
        r2.doors[dir.inverse().roomIndex] = door2
        r2.adjacentRooms[dir.inverse().roomIndex] = r1
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val settings = Settings.load()
            val files = configFiles()
            val cache = Cache.load(settings)
            AnimationDefinitions.init(AnimationDecoder().load(cache)).load(files.list(Settings["definitions.animations"]))
            GraphicDefinitions.init(GraphicDecoder().load(cache)).load(files.list(Settings["definitions.graphics"]))
            ItemDefinitions.init(ItemDecoder().load(cache)).load(files.list(Settings["definitions.items"]))
            NPCDefinitions.init(NPCDecoder(true).load(cache)).load(files.list(Settings["definitions.npcs"]))
            ObjectDefinitions.init(ObjectDecoder(true, lowDetail = false).load(cache)).load(files.list(Settings["definitions.objects"]))
            VariableDefinitions.load(files)
            Tables.load(files.list(Settings["definitions.tables"]))
            println("Generating dungeon...")

            val generator = DungeonGenerator(
                floor = 1,
                complexity = 6,
                size = DungeonSize.Small,
            )
            val start = System.currentTimeMillis()
            val dungeon = generator.generate()
            println("Took ${System.currentTimeMillis() - start}ms")

            println(dungeon.grid.filter { it != null && it.parent == null }.map { "${it?.type} ${it?.tile}" })
            dungeon.prettyPrint()
        }
    }
}
