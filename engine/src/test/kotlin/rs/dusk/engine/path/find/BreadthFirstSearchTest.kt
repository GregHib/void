package rs.dusk.engine.path.find

import io.mockk.mockk
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.index.Movement
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.path.obstruction.SmallObstruction
import rs.dusk.engine.path.target.RectangleTargetStrategy

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 20, 2020
 */
internal class BreadthFirstSearchTest {
    @Test
    fun `Path test`() {

        val finder = BreadthFirstSearch()

        // Given
        val start = Tile(1, 1)
        val size = Size(1, 1)

        val movement = Movement()

        val collisions: Collisions = mockk(relaxed = true)

        val strategy = RectangleTargetStrategy(collisions, Tile(4, 8), Size(1, 1))
        val obstruction = SmallObstruction(collisions)

        // When
        val result = finder.find(start, size, movement, strategy, obstruction)
        // Then
        println("Result: $result")
//        for (i in result - 1 downTo 0) {
//            println("Step ${finder.lastPathBufferX[i]} ${finder.lastPathBufferY[i]}")
//        }
    }
}