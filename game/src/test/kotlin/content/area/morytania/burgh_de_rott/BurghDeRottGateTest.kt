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
    fun `Open the gate into Burgh de Rott and walk through`() {
        val player = createPlayer(Tile(3484, 3245))
        val gate = GameObjects.find(Tile(3484, 3244), "burgh_de_rott_gate_west_closed")

        player.objectOption(gate, "Open")
        tick(3)

        assertNotNull(GameObjects.findOrNull(Tile(3485, 3242), "burgh_de_rott_gate_west_opened"))
        assertNotNull(GameObjects.findOrNull(Tile(3485, 3243), "burgh_de_rott_gate_east_opened"))

        player.walk(Tile(3484, 3243))
        tick(5)
        assertEquals(Tile(3484, 3243), player.tile)
    }
}
