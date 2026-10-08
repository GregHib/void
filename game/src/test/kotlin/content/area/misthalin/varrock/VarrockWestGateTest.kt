package content.area.misthalin.varrock

import WorldTest
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class VarrockWestGateTest : WorldTest() {

    @Test
    fun `Palace gate moves the player an extra tile from the south`() {
        val player = createPlayer(Tile(3245, 3501))
        val gate = GameObjects.find(Tile(3245, 3501), "gate_west_varrock_closed")

        player.objectOption(gate, "Open")
        tick(1)

        val opened = GameObjects.findOrNull(Tile(3245, 3502), 55443)
        assertNotNull(opened)
        assertEquals(0, opened.rotation)
        assertNull(GameObjects.findOrNull(Tile(3245, 3500), 55443))
        assertEquals(Tile(3245, 3501), player.tile)

        tickIf { player.tile != Tile(3245, 3503) }
        tick(1)

        assertEquals(Tile(3245, 3503), player.tile)
        assertEquals(Direction.NORTH, player.direction)
        assertNotNull(GameObjects.findOrNull(Tile(3245, 3501), "gate_west_varrock_closed"))
    }

    @Test
    fun `Palace gate moves the player an extra tile from the north`() {
        val player = createPlayer(Tile(3245, 3502))
        val gate = GameObjects.find(Tile(3245, 3501), "gate_west_varrock_closed")

        player.objectOption(gate, "Open")
        tick(1)

        val opened = GameObjects.findOrNull(Tile(3245, 3502), 55443)
        assertNotNull(opened)
        assertEquals(0, opened.rotation)
        assertEquals(Tile(3245, 3502), player.tile)

        tickIf { player.tile != Tile(3245, 3500) }
        tick(1)

        assertEquals(Tile(3245, 3500), player.tile)
        assertEquals(Direction.SOUTH, player.direction)
        assertNotNull(GameObjects.findOrNull(Tile(3245, 3501), "gate_west_varrock_closed"))
    }
}
