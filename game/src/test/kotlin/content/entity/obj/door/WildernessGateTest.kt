package content.entity.obj.door

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import walk
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class WildernessGateTest : WorldTest() {

    @Test
    fun `Open walk through and close the ice plateau gate`() {
        val player = createPlayer(Tile(2948, 3905))
        val gate = GameObjects.find(Tile(2948, 3904), "gate_29_closed")

        player.objectOption(gate, "Open")
        tick(3)

        val opened = GameObjects.findOrNull(Tile(2948, 3903), "gate_29_opened")
        assertNotNull(opened)
        assertNotNull(GameObjects.findOrNull(Tile(2947, 3903), "gate_30_opened"))

        player.walk(Tile(2948, 3902))
        tick(5)
        assertEquals(Tile(2948, 3902), player.tile)

        player.objectOption(opened, "Close")
        tick(3)

        assertNotNull(GameObjects.findOrNull(Tile(2948, 3904), "gate_29_closed"))
        assertNotNull(GameObjects.findOrNull(Tile(2947, 3904), "gate_30_closed"))
        assertFalse(player.containsMessage("won't budge"))
    }
}
