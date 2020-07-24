package rs.dusk.engine.path.find

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import rs.dusk.engine.model.entity.Direction.*
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.character.Movement
import rs.dusk.engine.model.entity.character.Steps
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.engine.path.TraversalStrategy

internal class AxisAlignmentIntegrationTest {

    lateinit var aa: AxisAlignment
    val size = Size.TILE

    @BeforeEach
    fun setup() {
        aa = spyk(AxisAlignment())
    }

    @TestFactory
    fun `Move diagonal towards target`() =
        arrayOf(
            Tile(-4, 0) to arrayOf(EAST, EAST, EAST, EAST),
            Tile(-3, 0) to arrayOf(EAST, EAST, EAST),
            Tile(-2, 0) to arrayOf(EAST, EAST),
            Tile(-1, 0) to arrayOf(EAST),
            Tile(-4, 1) to arrayOf(SOUTH_EAST, EAST, EAST, EAST),
            Tile(-3, 1) to arrayOf(SOUTH_EAST, EAST, EAST),
            Tile(-2, 1) to arrayOf(SOUTH_EAST, EAST),
            Tile(-1, 1) to arrayOf(SOUTH_EAST),
            Tile(-4, 2) to arrayOf(SOUTH_EAST, SOUTH_EAST, EAST, EAST),
            Tile(-3, 2) to arrayOf(SOUTH_EAST, SOUTH_EAST, EAST),
            Tile(-2, 2) to arrayOf(SOUTH_EAST, SOUTH_EAST),
            Tile(-1, 2) to arrayOf(SOUTH_EAST, SOUTH),
            Tile(-4, 3) to arrayOf(SOUTH_EAST, SOUTH_EAST, SOUTH_EAST, EAST),
            Tile(-3, 3) to arrayOf(SOUTH_EAST, SOUTH_EAST, SOUTH_EAST),
            Tile(-2, 3) to arrayOf(SOUTH_EAST, SOUTH_EAST, SOUTH),
            Tile(-1, 3) to arrayOf(SOUTH_EAST, SOUTH, SOUTH),
            Tile(-4, 4) to arrayOf(SOUTH_EAST, SOUTH_EAST, SOUTH_EAST, SOUTH_EAST),
            Tile(-3, 4) to arrayOf(SOUTH_EAST, SOUTH_EAST, SOUTH_EAST, SOUTH),
            Tile(-2, 4) to arrayOf(SOUTH_EAST, SOUTH_EAST, SOUTH, SOUTH),
            Tile(-1, 4) to arrayOf(SOUTH_EAST, SOUTH, SOUTH, SOUTH)
        ).map { (offset, expected) ->
            dynamicTest("Move $offset") {
                // Given
                val steps: Steps = mockk(relaxed = true)
                val target = Tile(10, 10)
                val strategy: TargetStrategy = mockk(relaxed = true)
                val traversal: TraversalStrategy = mockk(relaxed = true)
                val movement: Movement = mockk(relaxed = true)
                every { movement.steps } returns steps
                every { strategy.tile } returns target
                every { strategy.reached(target, size) } returns true
                val tile = target.add(offset)
                // When
                aa.find(tile, size, movement, strategy, traversal)
                // Then
                verify {
                    expected.forEach {
                        steps.add(it)
                    }
                }
            }
        }

    @TestFactory
    fun `Move diagonal around block target`() =
        arrayOf(
            Triple(Tile(-1, 2), Tile(-1, 1), arrayOf(EAST, SOUTH, SOUTH)),
            Triple(Tile(-2, 1), Tile(-1, 1), arrayOf(SOUTH, EAST, EAST)),
            Triple(Tile(-2, 3), Tile(-2, 2), arrayOf(EAST, SOUTH_EAST, SOUTH, SOUTH)),
            Triple(Tile(-3, 2), Tile(-2, 2), arrayOf(SOUTH, SOUTH_EAST, EAST, EAST))
        ).map { (offset, block, expected) ->
            dynamicTest("Move $offset") {
                // Given
                val steps: Steps = mockk(relaxed = true)
                val target = Tile(10, 10)
                val strategy: TargetStrategy = mockk(relaxed = true)
                val traversal: TraversalStrategy = mockk(relaxed = true)
                val movement: Movement = mockk(relaxed = true)
                every { movement.steps } returns steps
                every { strategy.tile } returns target
                val block = target.add(block)
                every { traversal.blocked(block.add(x = -1), SOUTH_EAST) } returns true
                every { traversal.blocked(block.add(x = -1), EAST) } returns true
                every { traversal.blocked(block.add(y = 1), SOUTH_EAST) } returns true
                every { traversal.blocked(block.add(y = 1), SOUTH) } returns true
                every { strategy.reached(target, size) } returns true
                val tile = target.add(offset)
                // When
                aa.find(tile, size, movement, strategy, traversal)
                // Then
                verify {
                    expected.forEach {
                        steps.add(it)
                    }
                }
            }
        }
}