package content.skill.summoning

import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit
import world.gregs.voidps.type.Tile

class BeastOfBurdenZeroSizeTest : WorldTest() {

    @Test
    fun `recovers from zero size beast of burden inventory`() {
        val player = createPlayer(Tile(3200, 3200))
        player.inventory.set(0, "coins", 10)
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)

        // Simulate stale zero-length inventory from before beast_of_burden was registered
        player.inventories.instances["beast_of_burden"] = Inventory.debug(0, id = "beast_of_burden")
        assertEquals(0, player.beastOfBurden.size)

        player.openBeastOfBurden()
        assertEquals(30, player.beastOfBurden.size)

        player.inventory.transaction {
            val moved = moveToLimit("coins", 5, player.beastOfBurden)
            assertEquals(5, moved)
        }
        assertEquals(5, player.beastOfBurden.count("coins"))
    }
}
