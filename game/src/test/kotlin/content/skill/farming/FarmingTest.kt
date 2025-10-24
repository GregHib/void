package content.skill.farming

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.test.assertEquals

class FarmingTest {

    @Test
    fun `Growth cycle`() {
        val farming = Farming(VariableDefinitions())
        val player = Player()
        val offset = 13
        val epoch = 1000 + offset
        val lastCycle = epoch - 10
        player["allotment_falador_nw"] = "weeds_0"
        farming.grow(player, epoch)
    }

    @Test
    fun `Weed growth cycle`() {
        val farming = Farming(VariableDefinitions())
        val player = Player()
        val offset = 13
        val epoch = 1000 + offset
        val lastCycle = epoch - 10
        player["allotment_falador_nw"] = "weeds_0"
        farming.grow(player, epoch)
        assertEquals("weeds_1", player["allotment_falador_nw", "weeds_0"])
        farming.grow(player, epoch)
        assertEquals("weeds_2", player["allotment_falador_nw", "weeds_0"])
    }
}
