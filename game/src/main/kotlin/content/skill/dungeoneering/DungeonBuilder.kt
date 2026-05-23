package content.skill.dungeoneering

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.RowDefinition
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
import java.util.LinkedList
import kotlin.random.nextInt

class DungeonBuilder(
    private val width: Int,
    private val height: Int,
    private val complexity: Int,
    private val floor: Int,
) {
    private val dungeon = arrayOfNulls<DungeonRoom>(width * height)
    private var critical = true

    private fun id(x: Int, y: Int) = x * width + y
    private fun id(tile: Tile) = tile.x * width + tile.y

    private fun theme(): String = when (floor) {
        in 1..11 -> "frozen"
        in 12..17, in 30..35 -> "abandoned"
        in 18..29 -> "furnished"
        in 36..47 -> "occult"
        in 48..60 -> "warped"
        else -> ""
    }

    private class DraftRoom(
        var door: DungeonDoor = DungeonDoor.UnallocatedDoor,
        var tile: Tile = Tile.EMPTY,
        val keys: MutableSet<String> = mutableSetOf(),
        val requires: Array<DoorRequirement> = Array(4) { DoorRequirement.Allowed },
        val doors: Array<DungeonDoor?> = arrayOfNulls(4),
        val adjacent: Array<DraftRoom?> = arrayOfNulls(4),
        val type: DungeonRoomType = DungeonRoomType.Normal,
        var zone: Zone = Zone.EMPTY,
    ) {
        override fun toString(): String {
            return buildString {
                append("DraftRoom(type=")
                append(type)
                append(", tile=$tile, doors=[")
                for (i in 0 until 4) {
                    append(Direction.cardinal[i])
                    append(" - ")
                    append(requires[i])
                    if (doors[i] != null) {
                        append(" ${doors[i]}")
                    }
                    if (adjacent[i] != null) {
                        append(" ${adjacent[i]!!.tile}")
                    }
                    if (i != 3) {
                        append(", ")
                    }
                }
                append("]")
                append(", keys=$keys")
            }
        }
    }

    enum class Size {
        Small,
        Medium,
        Large,
    }

    private fun criticalPath(size: Size): Array<DraftRoom> {
        val criticalRooms = when (size) {
            Size.Small -> random.nextInt(6..8)
            Size.Medium -> random.nextInt(10..14)
            Size.Large -> random.nextInt(19..23)
        }
        val doors = Array<DungeonDoor>(criticalRooms - 1) { DungeonDoor.UnallocatedDoor }
        val keyMap = mutableMapOf<Int, MutableSet<String>>()
        val keyCount = when (size) {
            Size.Small -> random.nextInt(2..3)
            Size.Medium -> random.nextInt(3..5)
            Size.Large -> random.nextInt(5..8)
        }

        var index = 1
        val groups = splitIntoGroups((0 until criticalRooms).toList().toTypedArray(), keyCount + 1)
        for (i in 1 until groups.size) {
            val group = groups[i]
            val first = group.first()
            val key = "key_${index++}"
            doors[first - 1] = DungeonDoor.KeyDoor(key)
            val randomRoom = groups[i - 1].last()
            keyMap.getOrPut(randomRoom) { mutableSetOf() }.add(key)
        }
        fun type(int: Int): DungeonRoomType {
            return when(int) {
                0 -> DungeonRoomType.Base
                criticalRooms - 1 -> DungeonRoomType.Boss
                else -> DungeonRoomType.Critical
            }
        }
        for (group in groups) {
            println(group.joinToString("") { "(${type(it)}${keyMap[it] ?: emptySet()})-[${doors.getOrNull(it)}]-" })
        }

        val dungeon = arrayOfNulls<DraftRoom>(width * height)

        // Build the DraftRoom objects indexed by their critical-path position
        val roomsByIndex = Array(criticalRooms) { i ->
            DraftRoom(
                door = if (i > 0) doors[i - 1] else DungeonDoor.UnallocatedDoor,
                keys = keyMap.getOrDefault(i, mutableSetOf()),
                type = type(i)
            )
        }
        println(roomsByIndex.joinToString("") { "(${it.type}${it.keys})-[${it.door}]-" })
        return roomsByIndex
//        println("===Complete===")
//

        // Track available expansion points: tiles reachable from placed rooms
        // Each entry is (tile, direction) meaning "room at tile has an open door in direction"
//        val availableStarts = mutableListOf<Pair<DraftRoom, Direction>>()
//        val stack = LinkedList<Pair<DraftRoom, Direction>>()

//        for ((groupIdx, group) in groups.withIndex()) {
//
//            val startTile: Tile
//            val startRoom = roomsByIndex[group.first()]
//
//            if (groupIdx == 0) {
//                // First group: place at a random tile
//                startTile = Tile(random.nextInt(width), random.nextInt(height))
//            } else {
//                // Subsequent groups: pick a random available expansion point
//                // availableStarts holds open (room, direction) pairs from previously placed rooms
//                check(availableStarts.isNotEmpty()) { "No available start positions for group $groupIdx" }
//                val (parentRoom, dir) = availableStarts.random(random)
//                availableStarts.remove(Pair(parentRoom, dir))
//                startTile = parentRoom.tile.add(dir)
//
//                // Link the section boundary: the locked door between parent and this room
//                linkRequirements(parentRoom, startRoom, dir)
//                link(parentRoom, startRoom, dir, startRoom.door)
//            }
//
//            stack.clear()
//            placeRoom(startRoom, startTile, dungeon, stack, boss = group.first() == criticalRooms - 1)
//            // After placing, drain any open directions into availableStarts for future groups
//            // but only after this group is fully placed, so we collect them at the end
//
//            // Place remaining rooms in this group via random walk
//            val groupRoomIndices = group.drop(1)
//            for (roomIdx in groupRoomIndices) {
//                check(stack.isNotEmpty()) { "Random walk ran out of space at room $roomIdx" }
//                val (parent, dir) = stack.pop()
//                stack.clear()
//                val room = roomsByIndex[roomIdx]
//                val tile = parent.tile.add(dir)
//
//                linkRequirements(parent, room, dir)
//                placeRoom(room, tile, dungeon, stack, boss = roomIdx == criticalRooms - 1)
//                link(parent, room, dir, room.door)
//            }
//
//            // All rooms in the group are placed; harvest open stack entries as future start candidates
//            availableStarts.addAll(stack)
//        }
//        return roomsByIndex.first().tile to dungeon
    }
    data class TreeNode(val tile: Tile, val children: MutableList<TreeNode> = mutableListOf(), var parent: TreeNode? = null) {
        val subtreeSize: Int get() = 1 + children.sumOf { it.subtreeSize }
    }
    private fun carveMazeTree(size: Int): Pair<Tile, Map<Tile, MutableList<Tile>>> {
        val tree = mutableMapOf<Tile, MutableList<Tile>>()
        val visited = HashSet<Int>()
        val start = Tile(random.nextInt(width), random.nextInt(height))

        // Recursive backtracker — guaranteed connected spanning tree, no dead ends until grid forces it
        val stack = LinkedList<Tile>()
        stack.push(start)
        visited.add(id(start))
        tree[start] = mutableListOf()

        while (stack.isNotEmpty() && visited.size < size) {
            val current = stack.peek()
            val neighbours = Direction.cardinal
                .filter { validDirection(current, it) }
                .map { current.add(it) }
                .filter { id(it) !in visited }
                .shuffled(random)

            if (neighbours.isEmpty()) {
                stack.pop() // backtrack
            } else {
                val next = neighbours.first()
                visited.add(id(next))
                tree[current]!!.add(next)
                tree[next] = mutableListOf()
                stack.push(next)
            }
        }

        return start to tree
    }

    private fun assignDoorsAndKeys(root: TreeNode, keyCount: Int, roomsByIndex: MutableMap<Tile, DraftRoom>) {
        // Step 1: extract spine by always following heaviest child
        val spine = mutableListOf<TreeNode>()
        var node = root
        while (true) {
            spine.add(node)
            node = node.children.maxByOrNull { it.subtreeSize } ?: break
        }

        // Step 2: pick keyCount cut points along the spine, spaced out
        val cutIndices = (1 until spine.size - 1)  // exclude base and boss
            .shuffled(random)
            .take(keyCount)
            .sorted()

        // Step 3: for each cut, assign lock to the spine room and key to a branch before it
        for (cutIdx in cutIndices) {
            val lockRoom = roomsByIndex[spine[cutIdx].tile]!!
            lockRoom.door = DungeonDoor.KeyDoor("key_$cutIdx")

            // Find branch nodes before this cut point that have off-spine children
            val branchCandidates = (0 until cutIdx)
                .flatMap { spine[it].children.filter { child -> child !in spine } }
                .flatMap { it.allLeaves() }  // put key at a leaf for maximum exploration

            val keyTile = (branchCandidates.ifEmpty { spine.take(cutIdx) }).random(random).tile
            roomsByIndex[keyTile]!!.keys.add("key_$cutIdx")
        }
    }

    fun TreeNode.allLeaves(): List<TreeNode> =
        if (children.isEmpty()) listOf(this) else children.flatMap { it.allLeaves() }

    private fun embedCriticalPath(roomsByIndex: Array<DraftRoom>): Array<DraftRoom?> {
        val dungeon = arrayOfNulls<DraftRoom>(width * height)
        val availableStarts = mutableListOf<Pair<DraftRoom, Direction>>()
        val stack = LinkedList<Pair<DraftRoom, Direction>>()
        var groupStart = true

        for ((i, room) in roomsByIndex.withIndex()) {
            if (groupStart) {
                stack.clear()
                val startTile: Tile
                if (availableStarts.isEmpty()) {
                    startTile = Tile(random.nextInt(width), random.nextInt(height))
                } else {
                    val (parentRoom, dir) = availableStarts.random(random)
                    availableStarts.remove(Pair(parentRoom, dir))
                    startTile = parentRoom.tile.add(dir)
                    linkRequirements(parentRoom, room, dir)
                    link(parentRoom, room, dir, room.door)
                }
                placeRoom(room, startTile, dungeon, stack, boss = i == roomsByIndex.lastIndex)
                groupStart = false
            } else {
                check(stack.isNotEmpty()) { "Random walk ran out of space at room $i" }
                val (parent, dir) = stack.pop()
                val tile = parent.tile.add(dir)
                linkRequirements(parent, room, dir)
                placeRoom(room, tile, dungeon, stack, boss = i == roomsByIndex.lastIndex)
                link(parent, room, dir, room.door)
            }

            // If the next room has a locked door, this group ends here
            val next = roomsByIndex.getOrNull(i + 1)
            if (next != null && next.door is DungeonDoor.KeyDoor) {
                availableStarts.addAll(stack)
                groupStart = true
            }
        }

        return dungeon
    }

    private fun criticalPath(size: Size, puzzles: Set<String>, skills: Map<Skill, Int>): Array<DraftRoom> {
        val roomCount = when (size) {
            Size.Small -> random.nextInt(6..8)
            Size.Medium -> random.nextInt(10..14)
            Size.Large -> random.nextInt(19..23)
        }
        val keyCount = when (size) {
            Size.Small -> random.nextInt(2..3)
            Size.Medium -> random.nextInt(3..5)
            Size.Large -> random.nextInt(5..8)
        }
        val rooms = Array(roomCount) { DraftRoom(type = if (it == 0) DungeonRoomType.Base else if (it == roomCount - 1) DungeonRoomType.Boss else DungeonRoomType.Critical) }

        var keyIndex = 1
        for (i in (1 until roomCount).shuffled().take(keyCount)) {
            val key = "key_${keyIndex++}"
            rooms[i].door = DungeonDoor.KeyDoor(key)
            rooms[i - 1].keys.add(key) // last room of previous section
        }
        for (i in (roomCount - 1) downTo 0) {
            val room = rooms[i]
            if (i == 0 || room.door != DungeonDoor.UnallocatedDoor) {
                continue
            }
            // Pick doors
            val door = randomDoor(
                skills,
                puzzles,
                mutableSetOf(),
                normalChance = if (i == roomCount - 1) 5 else 15,
                puzzleChance = if (i != roomCount - 1 && (complexity == 5 || complexity == 6)) 30 else 0,
                keyChance = 0//((i / roomCount.toDouble()) * 100.0).toInt()// Increase likelihood as fewer rooms remain
            )
            room.door = door
        }
        return rooms
    }

    private fun <T> splitIntoGroups(doors: Array<T>, numGroups: Int): List<Array<T>> {
        require(numGroups >= 1) { "Must have at least 1 group" }
        require(doors.size >= numGroups) { "Not enough doors (${ doors.size}) for $numGroups groups" }
        val splitPoints = (1 until doors.size)
            .shuffled()
            .take(numGroups - 1)
            .sorted()
        val boundaries = listOf(0) + splitPoints + listOf(doors.size)
        return boundaries.zipWithNext { start, end -> doors.sliceArray(start until end) }
    }

    private fun validDirection(tile: Tile, direction: Direction): Boolean {
        return when (direction) {
            Direction.NORTH -> tile.y < height - 1
            Direction.SOUTH -> tile.y > 0
            Direction.EAST -> tile.x < width - 1
            Direction.WEST -> tile.x > 0
            else -> false
        }
    }

    private fun populateDungeon(dungeon: Array<DraftRoom?>, start: Tile) {
        val draft = dungeon[id(start)]!!
        convert(dungeon, draft)
    }

    private fun convert(dungeon: Array<DraftRoom?>, draft: DraftRoom): DungeonRoom {
        val room = pickRoom(draft.tile.x, draft.tile.y, draft.requires, draft.type)
        if (room == null) {
            printMap()
            error("Unable to find room ${draft}")
        }
        this.dungeon[id(draft.tile)] = room
        room.keys.addAll(draft.keys)
        for (i in 0 until 4) {
            room.doors[i] = draft.doors[i]
            val adj = draft.adjacent[i] ?: continue
            val existing = this.dungeon[id(adj.tile)]
            room.adjacent[i] = existing ?: dungeon[id(adj.tile)]?.let { convert(dungeon, it) }
        }
        return room
    }

    private fun linkRequirements(parent: DraftRoom, child: DraftRoom, direction: Direction) {
        parent.requires[direction.index] = DoorRequirement.Required
        child.requires[direction.inverse().index] = DoorRequirement.Required
    }

    private fun link(parent: DraftRoom, child: DraftRoom, direction: Direction, door: DungeonDoor) {
        parent.doors[direction.index] = door
        parent.adjacent[direction.index] = child
        child.doors[direction.inverse().index] = DungeonDoor.NormalDoor
        child.adjacent[direction.inverse().index] = parent
    }

    private fun placeRoom(room: DraftRoom, tile: Tile, dungeon: Array<DraftRoom?>, stack: LinkedList<Pair<DraftRoom, Direction>>, boss: Boolean = false): DraftRoom {
        room.tile = tile
        dungeon[id(tile)] = room
        for (direction in Direction.cardinal) {
            // Block any out-of-map moves
            if (!validDirection(tile, direction)) {
                room.requires[direction.index] = DoorRequirement.Blocked
                continue
            }
            // Block shortcuts between adjacent rooms
            val tile = tile.add(direction)
            if (dungeon[id(tile)] != null) {
                room.requires[direction.index] = DoorRequirement.Blocked
                continue
            }
            // Boss room can't have any adjacent rooms
            if (boss && room.requires[direction.index] != DoorRequirement.Allowed) {
                room.requires[direction.index] = DoorRequirement.Blocked
                continue
            }
            stack.add(Pair(room, direction))
        }
        if (room.keys.isNotEmpty()) {
            stack.shuffle()
        }
        return room
    }


    private fun criticalPath(keys: MutableSet<String>, puzzles: Set<String>, skills: Map<Skill, Int>, roomCount: Int): Array<DraftRoom> {
        val rooms = Array(roomCount) { DraftRoom(type = if (it == 0) DungeonRoomType.Base else if (it == roomCount - 1) DungeonRoomType.Boss else DungeonRoomType.Critical) }

        val unallocatedKeys = mutableMapOf<String, Int>()
        for (i in (roomCount - 1) downTo 0) {
            val room = rooms[i]
            if (i == 0) {
                room.keys.addAll(unallocatedKeys.keys)
                unallocatedKeys.clear()
            } else if (unallocatedKeys.isNotEmpty()) {
                val key = unallocatedKeys.keys.random(random)
                val index = unallocatedKeys[key]!!
                val distance = index - i
                println("Place $key? Door=$index, this room =$i dist=$distance total=$roomCount")
                if (distance > 1 && random.nextBoolean()) {
                    unallocatedKeys.remove(key)
                    room.keys.add(key)
                }
            }
            if (i == 0) {
                continue
            }
            // Pick doors
            val door = randomDoor(
                skills,
                puzzles,
                keys,
                normalChance = if (i == roomCount - 1) 5 else 15,
                puzzleChance = if (i != roomCount - 1 && (complexity == 5 || complexity == 6)) 30 else 0,
                keyChance = ((i / roomCount.toDouble()) * 100.0).toInt()// Increase likelihood as fewer rooms remain
            )
            room.door = door
            if (door is DungeonDoor.KeyDoor) {
                unallocatedKeys[door.key] = i
            }
        }
        return rooms
    }

    private fun embedPath(criticalPath: Array<DraftRoom>, bonusKeys: MutableSet<String>, puzzles: Set<String>, skills: Map<Skill, Int>, bonusCount: Int): Array<DraftRoom?> {
        val dungeon = arrayOfNulls<DraftRoom>(width * height)
        val stack = LinkedList<Pair<DraftRoom, Direction>>()
        // Place critical path
        var index = 0
        val tile = Tile(random.nextInt(width), random.nextInt(height))
        placeRoom(criticalPath[index++], tile, dungeon, stack)
        while (index < criticalPath.size) {
            val (parent, dir) = stack.pop()
            val room = criticalPath[index++]
            val tile = parent.tile.add(dir)
            if (parent != room) {
                linkRequirements(parent, room, dir)
            }
            placeRoom(room, tile, dungeon, stack, index == criticalPath.lastIndex)
            if (parent != room) {
                link(parent, room, dir, room.door)
            }
        }
        val total = mutableListOf<DraftRoom>()
        // Bonus paths
//        while (total.size < bonusCount) {
//            val (parent, dir) = stack.pop()
//            val door = randomDoor(
//                skills,
//                puzzles,
//                bonusKeys,
//                normalChance = if (total.size == bonusCount - 1) 5 else 15,
//                puzzleChance = if (total.size != bonusCount - 1 && (complexity == 5 || complexity == 6)) 30 else 0,
//                keyChance = ((total.size / bonusCount.toDouble()) * 100.0).toInt()// Increase likelihood as fewer rooms remain
//            )
//            val tile = parent.tile.add(dir)
//            val room = DraftRoom(tile = tile)
//            link(parent, room, dir, door)
//            placeRoom(room, tile, dungeon, stack)
//            if (door is DungeonDoor.KeyDoor) { // TODO can bonus keys be placed on critical path?
//                // Place key somewhere earlier on the bonus path
//                val room = total.randomOrNull(random) ?: parent
//                room.keys.add(door.key)
//            }
//            total.add(room)
//        }
        return dungeon
    }
    /**
     * Constructs a valid critical path with [roomCount] number of rooms from (base) -> (boss)
     * Where keys for all looked doors are provided beforehand (no impossible situations)
     */
    private fun buildCriticalPath(keys: MutableSet<String>, puzzles: Set<String>, skills: Map<Skill, Int>, roomCount: Int): List<Pair<DungeonRoom, Direction>> {
        val previous = mutableListOf<DungeonRoom>()
        val emptyRooms = LinkedList<Pair<DungeonRoom, Direction>>()
        val base = createRoom(previous, roomCount, random.nextInt(width), random.nextInt(height), null)!!
        pushDoors(base, emptyRooms)

        while (emptyRooms.isNotEmpty() && previous.size < roomCount) {
            val (parent, direction) = emptyRooms.pop()
            // Pick a door
            val i = previous.size
            val door = randomDoor(
                skills,
                puzzles,
                keys,
                normalChance = if (i == roomCount - 1) 5 else 15,
                puzzleChance = if (i != 0 && i != roomCount - 1 && (complexity == 5 || complexity == 6)) 30 else 0,
                keyChance = ((previous.size / roomCount.toDouble()) * 100.0).toInt()// Increase likelihood as fewer rooms remain
            )
            if (door is DungeonDoor.KeyDoor) {
                // Backtrace to place valid key (do this rather than iterate backwards to allow multiple keys per room)
                val max = previous.indexOf(parent)
                val index = if (max == 0) 0 else (0 until max).random(random)
                previous[index].keys.add(door.key)
            }
            parent.doors[direction.index] = door
            // Create new room
            val x = parent.x + direction.delta.x
            val y = parent.y + direction.delta.y
            val room = createRoom(previous, roomCount, x, y, direction) ?: run {
                parent.doors[direction.index] = null
                continue
            }
            parent.adjacent[direction.index] = room
            room.doors[direction.inverse().index] = DungeonDoor.NormalDoor
            room.adjacent[direction.inverse().index] = parent
            // Queue empty rooms
            pushDoors(room, emptyRooms)
        }
        println(previous)
        return emptyRooms
    }

    private fun pushDoors(room: DungeonRoom, emptyRooms: LinkedList<Pair<DungeonRoom, Direction>>) {
        // Add all empty rooms, shuffle to avoid always picking the south most door
        for ((i, door) in room.doors.withIndex().shuffled()) {
            if (door is DungeonDoor.UnallocatedDoor && (room.adjacent[i] == null/* || room.adjacent[i]?.zone == Zone.EMPTY*/)) {
                emptyRooms.push(Pair(room, Direction.cardinal[i]))
            }
        }
        // FIXME don't shuffle but only allow to switch to others if the door isn't a normal one?
        //  Like there's no point creating an off shoot if there's nothing at the end (a key)
        if (random.nextInt(3) == 0) {
            // Occasionally shuffle so most recent room isn't always picked (to avoid boring straight paths)
            emptyRooms.shuffle()
        }
    }

    private fun createRoom(previous: MutableList<DungeonRoom>, roomCount: Int, x: Int, y: Int, direction: Direction?): DungeonRoom? {
//        require(dungeon[id(x, y)] == null) { "Room already exists at ${x}, $y" }
        val current = dungeon[id(x, y)]
        if (current != null) {
            printMap()
            print()
            error("Invalid room movement!")
        }
        val type = when (previous.size) {
            0 -> DungeonRoomType.Base
            roomCount - 1 -> DungeonRoomType.Boss
            else -> DungeonRoomType.Critical
        }
        val valid = validDoors(x, y)
        // Require the entry door
        if (direction != null) {
            valid[direction.inverse().index] = DoorRequirement.Required
        }
        // Block any neighbouring rooms to prevent short-cuts
        for ((i, req) in valid.withIndex()) {
            if (req == DoorRequirement.Required || req == DoorRequirement.Blocked) {
                continue
            }
            val dir = Direction.cardinal[i]
            dungeon[id(x + dir.delta.x, y + dir.delta.y)] ?: continue

            valid[i] = DoorRequirement.Blocked
        }
        val parent = pickRoom(x, y, valid, type) ?: return null
        dungeon[id(x, y)] = parent
//        for ((i, door) in parent.doors.withIndex()) {
//            if (door is DungeonDoor.UnallocatedDoor) {
//                val dir = Direction.cardinal[i]
//                parent.adjacent[i] = DungeonRoom(x + dir.delta.x, y + dir.delta.y, Zone.EMPTY)
//            }
//        }
        previous.add(parent)
        return parent
    }

    private fun randomDoor(maxLevels: Map<Skill, Int>, puzzles: Set<String>, keys: MutableSet<String>, normalChance: Int, puzzleChance: Int, keyChance: Int): DungeonDoor {
        val skillChance = 30
        val guardianChance = 15
        val chance = random.nextInt(normalChance + skillChance + keyChance + guardianChance)
        if (keys.isNotEmpty() && chance < keyChance) {
            val key = keys.random(random)
            keys.remove(key)
            return DungeonDoor.KeyDoor(key)
        }
        if (chance < keyChance + skillChance) {
            val skill = DungeonDoor.SkillDoor.skills.random(random)
            val level = maxLevels[skill] ?: 1
            val req = random.nextInt((level - 9).coerceAtLeast(1), level + 1)
            return DungeonDoor.SkillDoor(skill, req)
        }
        if (chance < keyChance + skillChance + puzzleChance) {
            val puzzle = puzzles.random(random)
            return DungeonDoor.PuzzleDoor(puzzle)
        }
        return if (chance < keyChance + skillChance + puzzleChance + guardianChance) DungeonDoor.GuardianDoor else DungeonDoor.NormalDoor
    }

    private fun pickRoom(x: Int, y: Int, required: Array<DoorRequirement>, type: DungeonRoomType = DungeonRoomType.Normal): DungeonRoom? {
        val theme = theme()
        val typeName = when (type) {
            DungeonRoomType.Normal, DungeonRoomType.Critical -> "normal"
            DungeonRoomType.Boss -> "boss"
            DungeonRoomType.Base -> "base"
        }
        val validRooms = mutableListOf<Pair<RowDefinition, Int>>()
        for (complexity in complexity downTo 1) {
            for (row in Tables.getOrNull("${theme}_c${complexity}_${typeName}")?.rows() ?: continue) {
                if (floor in 12..17) {
                    // TODO skip seeker sentinel puzzle, balak pummeller and shadow forgr ihlakhizan bosses
                }
                val doors = row.boolList("doors")
                for (rotation in 0 until 4) {
                    if (isValidRotation(doors, required, rotation, false)) {
                        validRooms.add(Pair(row, rotation))
                    }
                }
            }
        }
        val (pick, rotation) = validRooms.randomOrNull(random) ?: return null
        val zone = Zone(pick.int("x"), pick.int("y"))
        val doors = pick.boolList("doors")
        val room = DungeonRoom(x, y, zone, type = type, rotation = rotation)
        for (i in 0 until 4) {
            val index = (i + rotation) % 4
            if (doors[i]) {
                room.doors[index] = DungeonDoor.UnallocatedDoor
            }
        }
        return room
    }

    fun isValidRotation(
        roomDoors: List<Boolean>,
        required: Array<DoorRequirement>,
        rotation: Int,
        isPuzzleRoom: Boolean,
    ): Boolean {
        for (i in 0 until 4) {
            val roomSlot = roomDoors[(i - rotation + 4) % 4]
            val req = required[i]
            when (req) {
                DoorRequirement.Blocked if roomSlot -> return false
                DoorRequirement.Required if !roomSlot -> return false
                // Puzzle room south doors must not align with the required door (as we're traversing backwards)
                DoorRequirement.Required if isPuzzleRoom && (i - rotation + 4) % 4 != Direction.SOUTH.index -> return false
                else -> {}
            }
        }
        return true
    }

    private fun validDoors(x: Int, y: Int): Array<DoorRequirement> {
        val doors = Array(4) { DoorRequirement.Blocked }
        if (x != 0) {
            doors[Direction.WEST.index] = DoorRequirement.Allowed
        }
        if (y != height - 1) {
            doors[Direction.NORTH.index] = DoorRequirement.Allowed
        }
        if (x != width - 1) {
            doors[Direction.EAST.index] = DoorRequirement.Allowed
        }
        if (y != 0) {
            doors[Direction.SOUTH.index] = DoorRequirement.Allowed
        }
        return doors
    }


    fun build() {
        println()
        val puzzles = setOf("puzzle_a", "puzzle_b")
//        val path = criticalPath(mutableSetOf("key_a", "key_b", "key_c"), puzzles, mapOf(), 6)
        val path = criticalPath(Size.Small, puzzles, mapOf())
        println("=== Critical path===")
        println(buildString {
            for (room in path) {
                val door = room.door
                if (door != DungeonDoor.UnallocatedDoor) {
                    append("-[")
                    when (door) {
                        DungeonDoor.GuardianDoor -> append("Guardian")
                        is DungeonDoor.KeyDoor -> append(door.key)
                        is DungeonDoor.PuzzleDoor -> append("Puzzle")
                        is DungeonDoor.SkillDoor -> append("${door.level} ${door.skill.name.lowercase()}")
                        else -> {}
                    }
                    append("]->")
                }
                append("(")
                if (room.type == DungeonRoomType.Base) {
                    append("base ")
                } else if (room.type == DungeonRoomType.Boss) {
                    append("boss")
                }
                if (room.keys.isNotEmpty()) {
                    append(room.keys.joinToString())
                }
                append(")")
            }
        })
        for (room in path) {
            println("${room.door} ${room}")
        }
        println("=== Embedded Path ==")
//        val criticalPath = criticalPath(Size.Small) // path
        val embedded = embedCriticalPath(path)
        //embedPath(path, mutableSetOf("key_d", "key_e"), puzzles, mapOf(), 4)
        for (room in embedded) {
            if (room == null) {
                continue
            }
            println(room)
        }
        populateDungeon(embedded, path.first().tile)
//        buildCriticalPath(mutableSetOf("key_a", "key_b", "key_c"), setOf("puzzle_a", "puzzle_b"), mapOf(), 6)
    }

    private val Direction.index: Int
        get() = ordinal / 2

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

            val builder = DungeonBuilder(4, 4, 3, 1)
            builder.build()
            builder.printMap()
            builder.print()
        }
    }

    fun print() {
        for (room in dungeon) {
            if (room == null) continue
            println(room)
        }
    }

    fun printMap() {
        for (y in (height - 1) downTo 0) {
            // Each room "row" consists of 5 lines of text
            val line1 = StringBuilder() // Top border + North Door
            val line2 = StringBuilder() // Padding
            val line3 = StringBuilder() // West Door + Room Icon + East Door
            val line4 = StringBuilder() // Padding
            val line5 = StringBuilder() // Bottom border + South Door

            for (x in 0 until width) {
                val room = dungeon[id(x, y)]

                if (room == null) {
                    // Empty space in the grid
                    val emptySpace = "           " // 9 spaces
                    line1.append(emptySpace)
                    line2.append(emptySpace)
                    line3.append("     .     ") // Center dot for orientation
                    line4.append(emptySpace)
                    line5.append(emptySpace)
                } else {
                    // Determine Door Labels
                    val nDoor = getHorizDoor(room.doors[0])
                    val eDoor = getVertDoor(room.doors[1])
                    val sDoor = getHorizDoor(room.doors[2])
                    val wDoor = getVertDoor(room.doors[3])

                    val icon = getRoomIcon(room) // 3 chars wide
                    val info = getRoomInfo(room) // 3 chars wide

                    // Build the 5 lines for this specific room
                    line1.append("+--$nDoor--+")
                    line2.append("|  $icon   |")
                    line3.append("|$wDoor $eDoor|")
                    line4.append("| $info |")
                    line5.append("+--$sDoor--+")
                }
            }

            // Print the 5-line block for this row
            println(line1)
            println(line2)
            println(line3)
            println(line4)
            println(line5)
        }
    }

    // Returns a 3-character string for horizontal walls/doors
    private fun getHorizDoor(door: DungeonDoor?): String {
        return when (door) {
            null -> "-----" // Wall
            is DungeonDoor.NormalDoor, is DungeonDoor.UnallocatedDoor -> "DOOR-" // Open path
            is DungeonDoor.KeyDoor -> "-${door.key.uppercase().replace("_", "")}"
            is DungeonDoor.GuardianDoor -> "GUARD"
            is DungeonDoor.SkillDoor -> "SKILL"
            is DungeonDoor.PuzzleDoor -> "PUZZL"
        }
    }

    // Returns a 1-character string for vertical walls/doors
    private fun getVertDoor(door: DungeonDoor?): String? {
        return when (door) {
            is DungeonDoor.NormalDoor, is DungeonDoor.UnallocatedDoor -> "DOOR" // Open path
            is DungeonDoor.KeyDoor -> door.key.uppercase().replace("_", "")
            is DungeonDoor.GuardianDoor -> "GUAR"
            is DungeonDoor.SkillDoor -> "SKIL"
            is DungeonDoor.PuzzleDoor -> "PUZZ"
            else -> "    "
        }
    }

    // Returns a 3-character icon for the room type
    private fun getRoomIcon(room: DungeonRoom): String {
        return when (room.type) {
            DungeonRoomType.Normal -> "NORM"
            DungeonRoomType.Base -> "BASE"
            DungeonRoomType.Boss -> "BOSS"
            DungeonRoomType.Critical -> "    "//"CRIT"
        }
    }

    private fun getRoomInfo(room: DungeonRoom): String {
        return when {
            room.keys.isNotEmpty() -> buildString {
                append("KEY ")
                append(room.keys.joinToString(",") { it.removePrefix("key_").uppercase() })
                while (length < 7) {
                    append(" ")
                }
            }.take(7)
            else -> "       "
        }
    }
}
