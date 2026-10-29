package content.area.asgarnia.port_sarim

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

class LadyLumbridgeTest : WorldTest() {

    @Test
    fun `Cross the gangplank onto the Lady Lumbridge`() {
        val player = createPlayer(Tile(3047, 3204))
        val gangplank = GameObjects.find(Tile(3047, 3205), "port_sarim_lady_lumbridge_gangplank_enter")

        player.objectOption(gangplank, "Cross")
        tickIf(limit = 20) { player.tile != Tile(3047, 3207, 1) }

        assertEquals(Tile(3047, 3207, 1), player.tile)
    }

    @Test
    fun `Cross the gangplank off the Lady Lumbridge`() {
        val player = createPlayer(Tile(3047, 3207, 1))
        val gangplank = GameObjects.find(Tile(3047, 3206, 1), "port_sarim_lady_lumbridge_gangplank_exit")

        player.objectOption(gangplank, "Cross")
        tickIf(limit = 20) { player.tile != Tile(3047, 3204) }

        assertEquals(Tile(3047, 3204), player.tile)
    }
}
