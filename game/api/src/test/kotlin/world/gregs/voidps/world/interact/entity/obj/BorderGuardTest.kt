package world.gregs.voidps.world.interact.entity.obj

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.WalkHandler
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.script.WorldTest

internal class BorderGuardTest : WorldTest() {

    private lateinit var collision: Collisions
    private val handler = WalkHandler()


    @BeforeEach
    fun start() {
        collision = get()
    }

    @Test
    fun `Can walk west through a vertical border`() {
        val player = createPlayer("player", Tile(3112, 3420))
        tick()

        handler.validate(player, Walk(3106, 3421))
        tick(8)

        assertEquals(Tile(3106, 3421), player.tile)
    }

    @Test
    fun `Can walk east through a vertical border`() {
        val player = createPlayer("player", Tile(3106, 3421))
        tick()

        handler.validate(player, Walk(3112, 3420))
        tick(8)

        assertEquals(Tile(3112, 3420), player.tile)
    }

    @Test
    fun `Can walk south through a horizontal border`() {
        val player = createPlayer("player", Tile(3292, 3387))
        tick()

        handler.validate(player, Walk(3293, 3383))
        tick(6)

        assertEquals(Tile(3293, 3383), player.tile)
    }

    @Test
    fun `Can walk north through a horizontal border`() {
        val player = createPlayer("player", Tile(3293, 3383))
        tick()

        handler.validate(player, Walk(3292, 3387))
        tick(6)

        assertEquals(Tile(3292, 3387), player.tile)
    }

}