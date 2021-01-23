package world.gregs.voidps.engine.path.algorithm

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.move.Steps
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.TargetStrategy
import world.gregs.voidps.engine.path.TraversalStrategy

/**
 * @author GregHib <greg@gregs.world>
 * @since August 17, 2020
 */
internal class DirectDiagonalSearchTest {

    lateinit var dd: DirectDiagonalSearch

    @BeforeEach
    fun setup() {
        dd = spyk(DirectDiagonalSearch())
    }

    val size = Size.TILE

    @TestFactory
    fun `Move towards target`() = arrayOf(
        Tile(11, 9) to Direction.NORTH_WEST,
        Tile(10, 9) to Direction.NORTH,
        Tile(9, 9) to Direction.NORTH_EAST,
        Tile(9, 10) to Direction.EAST,
        Tile(9, 11) to Direction.SOUTH_EAST,
        Tile(10, 11) to Direction.SOUTH,
        Tile(11, 11) to Direction.SOUTH_WEST,
        Tile(11, 10) to Direction.WEST
    ).map { (tile, dir) ->
        dynamicTest("Move $dir to $tile") {
            // Given
            val steps: Steps = mockk(relaxed = true)
            val target = Tile(10, 10)
            val strategy: TargetStrategy = mockk(relaxed = true)
            val traversal: TraversalStrategy = mockk(relaxed = true)
            val movement: Movement = mockk(relaxed = true)
            every { movement.steps } returns steps
            every { strategy.tile } returns target
            // When
            val result = dd.find(tile, size, movement, strategy, traversal)
            // Then
            result as PathResult.Partial
            verify {
                steps.add(dir)
            }
        }
    }

    @TestFactory
    fun `Blocked diagonal moves horizontal`() = arrayOf(
        Tile(11, 9) to Direction.WEST,
        Tile(9, 9) to Direction.EAST,
        Tile(9, 11) to Direction.EAST,
        Tile(11, 11) to Direction.WEST
    ).map { (tile, dir) ->
        dynamicTest("Move $dir to $tile") {
            // Given
            val steps: Steps = mockk(relaxed = true)
            val target = Tile(10, 10)
            val strategy: TargetStrategy = mockk(relaxed = true)
            val traversal: TraversalStrategy = mockk(relaxed = true)
            val movement: Movement = mockk(relaxed = true)
            every { movement.steps } returns steps
            every { strategy.tile } returns target
            every { traversal.blocked(11, 11, tile.plane, Direction.SOUTH_WEST) } returns true
            every { traversal.blocked(9, 9, tile.plane, Direction.NORTH_EAST) } returns true
            every { traversal.blocked(9, 11, tile.plane, Direction.SOUTH_EAST) } returns true
            every { traversal.blocked(11, 9, tile.plane, Direction.NORTH_WEST) } returns true
            // When
            val result = dd.find(tile, size, movement, strategy, traversal)
            // Then
            result as PathResult.Partial
            verify {
                steps.add(dir)
            }
        }
    }

    @TestFactory
    fun `Blocked diagonal and horizontal moves vertical`() = arrayOf(
        Tile(11, 9) to Direction.NORTH,
        Tile(9, 9) to Direction.NORTH,
        Tile(9, 11) to Direction.SOUTH,
        Tile(11, 11) to Direction.SOUTH
    ).map { (tile, dir) ->
        dynamicTest("Move $dir to $tile") {
            // Given
            val steps: Steps = mockk(relaxed = true)
            val target = Tile(10, 10)
            val strategy: TargetStrategy = mockk(relaxed = true)
            val traversal: TraversalStrategy = mockk(relaxed = true)
            val movement: Movement = mockk(relaxed = true)
            every { movement.steps } returns steps
            every { strategy.tile } returns target
            every { traversal.blocked(11, 11, tile.plane, Direction.SOUTH_WEST) } returns true
            every { traversal.blocked(11, 11, tile.plane, Direction.WEST) } returns true
            every { traversal.blocked(9, 9, tile.plane, Direction.NORTH_EAST) } returns true
            every { traversal.blocked(9, 9, tile.plane, Direction.EAST) } returns true
            every { traversal.blocked(9, 11, tile.plane, Direction.SOUTH_EAST) } returns true
            every { traversal.blocked(9, 11, tile.plane, Direction.EAST) } returns true
            every { traversal.blocked(11, 9, tile.plane, Direction.NORTH_WEST) } returns true
            every { traversal.blocked(11, 9, tile.plane, Direction.WEST) } returns true
            // When
            val result = dd.find(tile, size, movement, strategy, traversal)
            // Then
            result as PathResult.Partial
            verify {
                steps.add(dir)
            }
        }
    }
}