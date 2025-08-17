package content.area.misthalin

import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Tile

internal class BorderGuardTest : WorldTest() {

    private lateinit var collision: Collisions
    private lateinit var handler: InstructionHandlers

    @BeforeEach
    fun start() {
        handler = get()
        collision = get()
    }

    @Test
    fun `Can walk west through a vertical border`() {
        val player = createPlayer(Tile(3112, 3420))
        tick()

        handler.walk(Walk(3106, 3421), player)
        tick(8)

        assertEquals(Tile(3106, 3421), player.tile)
    }

    @Test
    fun `Can walk east through a vertical border`() {
        val player = createPlayer(Tile(3106, 3421))
        tick()

        handler.walk(Walk(3112, 3420), player)
        tick(8)

        assertEquals(Tile(3112, 3420), player.tile)
    }

    @Test
    fun `Can walk south through a horizontal border`() {
        val player = createPlayer(Tile(3292, 3387))
        tick()

        handler.walk(Walk(3293, 3383), player)
        tick(6)

        assertEquals(Tile(3293, 3383), player.tile)
    }

    @Test
    fun `Can walk north through a horizontal border`() {
        val player = createPlayer(Tile(3293, 3383))
        tick()

        handler.walk(Walk(3292, 3387), player)
        tick(6)

        assertEquals(Tile(3292, 3387), player.tile)
    }
}
