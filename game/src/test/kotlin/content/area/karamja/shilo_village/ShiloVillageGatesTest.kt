package content.area.karamja.shilo_village

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

class ShiloVillageGatesTest : WorldTest() {

    @Test
    fun `Metal gate lets players through and closes behind them`() {
        val player = createPlayer(Tile(2876, 2952))
        val gate = GameObjects.find(Tile(2875, 2952), "metal_gate_closed")

        player.objectOption(gate, "Open")
        tick(8)

        assertEquals(Tile(2874, 2952), player.tile)
        assertNotNull(GameObjects.findOrNull(Tile(2875, 2952), "metal_gate_closed"))
    }

    @Test
    fun `Wooden gate lets players through and closes behind them`() {
        val player = createPlayer(Tile(2866, 2953))
        val gate = GameObjects.find(Tile(2867, 2953), "wooden_gate_closed")

        player.objectOption(gate, "Open")
        tick(8)

        assertEquals(Tile(2868, 2953), player.tile)
        assertNotNull(GameObjects.findOrNull(Tile(2867, 2953), "wooden_gate_closed"))
    }
}
