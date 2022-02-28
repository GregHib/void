package world.gregs.voidps.engine.path.traverse

import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.strategy.CharacterCollision

internal class TraversalIntegrationTest {

    lateinit var collisions: Collisions
    lateinit var traversal: SmallTraversal
    lateinit var collision: CollisionStrategy

    @BeforeEach
    fun setup() {
        mockkStatic("world.gregs.voidps.engine.map.collision.CollisionsKt")
        collisions = spyk(Collisions())
        traversal = spyk(SmallTraversal)
        collision = CharacterCollision(collisions)
    }

    @Test
    fun `Diagonal corner`() {
        // Given
        val start = Tile(0, 0)
        val direction = Direction.NORTH_EAST
        collisions[0, 1, 0] = 671170720
        collisions[0, 0, 0] = 8389634
        // When
        val result = traversal.blocked(collision, start, Size.ONE, direction)
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
        // When
        val result = traversal.blocked(collision, start, Size.ONE, direction)
        // Then
        assertFalse(result)
    }
}