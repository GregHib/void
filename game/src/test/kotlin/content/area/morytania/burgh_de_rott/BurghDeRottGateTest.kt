package content.area.morytania.burgh_de_rott

import WorldTest
import objectOption
import org.junit.jupiter.api.Test
import walk
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BurghDeRottGateTest : WorldTest() {

    @Test
    fun `Gate closes behind the player once they walk through`() {
        val player = createPlayer(Tile(3484, 3245))
        val gate = GameObjects.find(Tile(3484, 3244), "burgh_de_rott_gate_west_closed")

        player.objectOption(gate, "Open")
        tick(2)

        assertNotNull(GameObjects.findOrNull(Tile(3485, 3242), "burgh_de_rott_gate_west_opened"))
        assertNotNull(GameObjects.findOrNull(Tile(3485, 3243), "burgh_de_rott_gate_east_opened"))

        tick(6)

        assertEquals(Tile(3484, 3243), player.tile)
        assertNotNull(GameObjects.findOrNull(Tile(3484, 3244), "burgh_de_rott_gate_west_closed"))
        assertNotNull(GameObjects.findOrNull(Tile(3485, 3244), "burgh_de_rott_gate_east_closed"))
    }

    @Test
    fun `Other players can't follow through the open gate`() {
        val player = createPlayer(Tile(3484, 3245))
        val other = createPlayer(Tile(3485, 3244))
        val gate = GameObjects.find(Tile(3484, 3244), "burgh_de_rott_gate_west_closed")

        player.objectOption(gate, "Open")
        tick(2)

        // The gate is open but never gave up its collision, so the doorway is still blocked
        assertNotNull(GameObjects.findOrNull(Tile(3485, 3243), "burgh_de_rott_gate_east_opened"))
        other.walk(Tile(3485, 3243))
        tick(1)

        assertEquals(Tile(3485, 3244), other.tile)
    }
}
