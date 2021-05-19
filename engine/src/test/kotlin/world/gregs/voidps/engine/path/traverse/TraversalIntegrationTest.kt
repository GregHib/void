package world.gregs.voidps.engine.path.traverse

import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.set
import world.gregs.voidps.engine.path.TraversalType

internal class TraversalIntegrationTest {

    lateinit var collisions: Collisions
    lateinit var traversal: SmallTraversal

    @BeforeEach
    fun setup() {
        mockkStatic("world.gregs.voidps.engine.map.collision.CollisionsKt")
        collisions = spyk(Collisions())
        traversal = spyk(SmallTraversal(TraversalType.Land, true, collisions))
    }

    @Test
    fun `Diagonal corner`() {
        // Given
        val start = Tile(0, 0)
        val direction = Direction.NORTH_EAST
        collisions[0, 1, 0] = 671170720
        collisions[0, 0, 0] = 8389634
        // When
        val result = traversal.blocked(start.x, start.y, start.plane, direction)
        // Then
        assertTrue(result)
    }

    @Test
    fun `Around corner`() {
        // Given
        val start = Tile(3091, 3487)
        val direction = Direction.WEST
        val collisions = Collisions()
        collisions[3091, 3487, 0] = 8389634
        collisions[3090, 3487, 0] = 16779268
        val traversal = SmallTraversal(TraversalType.Land, true, collisions)
        // When
        val result = traversal.blocked(start.x, start.y, start.plane, direction)
        // Then
        assertFalse(result)
    }
}