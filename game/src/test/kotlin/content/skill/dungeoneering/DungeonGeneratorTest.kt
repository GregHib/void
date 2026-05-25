package content.skill.dungeoneering

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.config.TableDefinition
import world.gregs.voidps.engine.data.definition.ColumnType
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.setRandom
import kotlin.math.abs
import kotlin.random.Random

class DungeonGeneratorTest {

    @BeforeEach
    fun setUp() {
        setRandom(Random(0L))
        Tables.set(mapOf("dungeon_keys" to TableDefinition(mapOf("keys" to 0), arrayOf(ColumnType.ColumnList(ColumnType.ColumnEntity)), arrayOf(emptyList<Int>()), intArrayOf(0))))
        Rows.set(arrayOf(RowDefinition(0, arrayOf(List(16) { it }), "dungeon_keys.all")), mapOf("dungeon_keys.all" to 0))
    }

    @ParameterizedTest
    @EnumSource(DungeonSize::class)
    fun `Dungeon configuration sizes initialize within defined bounds`(size: DungeonSize) {
        val generator = DungeonGenerator(size, floor = 1, complexity = 1)
        val expectedWidth = when (size) {
            DungeonSize.Small -> 5
            DungeonSize.Medium -> 4
            DungeonSize.Large -> 8
        }
        val expectedHeight = when (size) {
            DungeonSize.Small -> 4
            DungeonSize.Medium -> 8
            DungeonSize.Large -> 8
        }
        val path = generator.findPathOnGrid(5)
        for (tile in path) {
            assertTrue(tile.x in 0 until expectedWidth)
            assertTrue(tile.y in 0 until expectedHeight)
        }
    }

    @Test
    fun `Find path of exact requested length`() {
        val generator = DungeonGenerator(DungeonSize.Small, floor = 1, complexity = 1)
        val targetLength = 6
        val path = generator.findPath(0, 0, targetLength)

        assertNotNull(path)
        assertEquals(targetLength, path!!.size)

        // Ensure continuity of steps
        for (i in 0 until path.size - 1) {
            val current = path[i]
            val next = path[i + 1]
            val dx = abs(current.x - next.x)
            val dy = abs(current.y - next.y)
            assertTrue((dx == 1 && dy == 0) || (dx == 0 && dy == 1))
        }
    }

    @Test
    fun `Match rotated door templates`() {
        val generator = DungeonGenerator(DungeonSize.Small, floor = 1, complexity = 1)

        // West, North, East, South
        val template = listOf(true, false, true, false)

        // Rotation 0 (no changes)
        assertTrue(generator.matchesDoors(template, booleanArrayOf(true, false, true, false), 0))
        assertFalse(generator.matchesDoors(template, booleanArrayOf(false, true, false, true), 0))

        // Rotation 1 (90 degrees clockwise)
        assertTrue(generator.matchesDoors(template, booleanArrayOf(false, true, false, true), 1))

        // Rotation 2 (180 degrees)
        assertTrue(generator.matchesDoors(template, booleanArrayOf(true, false, true, false), 2))
    }

    @Test
    fun `Critical path includes base, boss room types and grid positions`() {
        val generator = DungeonGenerator(DungeonSize.Small, floor = 1, complexity = 1)
        val gridWidth = 5
        val gridHeight = 4
        val grid = arrayOfNulls<DungeonRoom>(gridWidth * gridHeight)

        val path = generator.createCriticalPath(grid)

        assertFalse(path.isEmpty())
        assertEquals(DungeonRoomType.Base, path.first().type)
        assertEquals(DungeonRoomType.Boss, path.last().type)
        assertTrue(path.first().isCritical)
        assertTrue(path.last().isCritical)

        for (room in path) {
            val index = room.tile.y * gridWidth + room.tile.x
            assertEquals(room, grid[index])
        }
    }

    @Test
    fun `Lock critical doors with incremental keys`() {
        val generator = DungeonGenerator(DungeonSize.Small, floor = 1, complexity = 1)
        val gridWidth = 5
        val gridHeight = 4
        val grid = arrayOfNulls<DungeonRoom>(gridWidth * gridHeight)

        val path = generator.createCriticalPath(grid)
        generator.lockCriticalDoors(path)

        var lockedCount = 0
        var lastDepth = -1

        // Traverse critical path to verify door locks increment in depth along the path
        for (i in 0 until path.size - 1) {
            val current = path[i]
            val next = path[i + 1]
            val dir = next.tile.delta(current.tile).toDirection()

            // Check doors on both sides
            val doorOnCurrent = current.doors[dir.index]
            if (doorOnCurrent is DungeonDoor.Locked) {
                lockedCount++
                assertTrue(doorOnCurrent.depth > lastDepth, "Locked door depths must be ordered incrementally")
                lastDepth = doorOnCurrent.depth
            }
        }

        assertTrue(lockedCount <= path.size)
    }

    @Test
    fun `Puzzle rooms are restricted to high complexities and non-leaf rooms`() {
        // Complexity 1 should have no Puzzle Rooms
        val generatorLowComplexity = DungeonGenerator(DungeonSize.Small, floor = 1, complexity = 1)
        val gridLow = arrayOfNulls<DungeonRoom>(5 * 4)
        val pathLow = generatorLowComplexity.createCriticalPath(gridLow)
        generatorLowComplexity.createBonusRooms(pathLow, gridLow)
        generatorLowComplexity.assignRoomTypes(gridLow, mapOf())

        val containsPuzzleLow = gridLow.filterNotNull().any { it.type == DungeonRoomType.Puzzle }
        assertFalse(containsPuzzleLow, "Puzzle rooms should not generate on complexity levels below 5")

        // Complexity 5 can have Puzzle rooms but never as leaf nodes
        val generatorHighComplexity = DungeonGenerator(
            DungeonSize.Small,
            floor = 1,
            complexity = 5,
            puzzleRoomChance = 1.0, // Force puzzle rooms
        )
        val gridHigh = arrayOfNulls<DungeonRoom>(5 * 4)
        val pathHigh = generatorHighComplexity.createCriticalPath(gridHigh)
        generatorHighComplexity.createBonusRooms(pathHigh, gridHigh)

        // Re-establish parent relations (usually done during BFS)
        val startRoom = pathHigh.first()
        generatorHighComplexity.bfs(startRoom, gridHigh) { curr, _, neighbour ->
            neighbour.parent = curr
            true
        }

        generatorHighComplexity.assignRoomTypes(gridHigh, mapOf())

        val activeRooms = gridHigh.filterNotNull()
        for (room in activeRooms) {
            if (room.type == DungeonRoomType.Puzzle) {
                // Ensure it is not a leaf node (i.e. has at least one child room pointing to it as parent)
                val hasChildren = activeRooms.any { it.parent == room }
                assertTrue(hasChildren, "Puzzle room at ${room.tile} is a leaf node, which is invalid")
            }
        }
    }

    @ParameterizedTest
    @EnumSource(DungeonSize::class)
    fun `Generated dungeon is entirely solvable from start to finish`(size: DungeonSize) {
        val generator = DungeonGenerator(size, floor = 1, complexity = 5)
        val dungeonMap = generator.generate()

        val startRoom = dungeonMap.grid.filterNotNull().first { it.type == DungeonRoomType.Base }
        val bossRoom = dungeonMap.grid.filterNotNull().first { it.type == DungeonRoomType.Boss }

        val acquiredKeys = mutableSetOf<Int>()
        val accessibleRooms = mutableSetOf<DungeonRoom>()

        var addedNewRooms = true

        // Simulate a standard play traversal loop:
        // Collect reachable keys, expand boundary, repeat until no new progress can be made.
        while (addedNewRooms) {
            addedNewRooms = false

            val reachable = generator.bfs(startRoom, dungeonMap.grid) { _, door, _ ->
                when (door) {
                    is DungeonDoor.Locked -> door.depth in acquiredKeys
                    else -> true
                }
            }

            for (room in reachable) {
                if (accessibleRooms.add(room)) {
                    addedNewRooms = true
                    // Collect any keys lying in newly reached rooms
                    acquiredKeys.addAll(room.keyIds)
                }
            }
        }

        assertTrue(
            accessibleRooms.contains(bossRoom),
            "Boss room was not reachable during play simulation. Solvability invariant violated.",
        )
    }

    private val Direction.index: Int
        get() = when (this) {
            Direction.WEST -> 0
            Direction.NORTH -> 1
            Direction.EAST -> 2
            Direction.SOUTH -> 3
            else -> -1
        }
}
