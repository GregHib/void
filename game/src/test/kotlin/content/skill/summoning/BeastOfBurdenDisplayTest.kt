package content.skill.summoning

import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class BeastOfBurdenDisplayTest : WorldTest() {

    @Test
    fun `beast of burden interface binds items component`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)
        player.openBeastOfBurden()
        val component = InterfaceDefinitions.getComponent("beast_of_burden", "items")
        requireNotNull(component)
        assertEquals("beast_of_burden", component["inventory", ""])
        assertEquals("items", component.stringId)
    }
}
