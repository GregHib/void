package content.skill.summoning

import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit
import world.gregs.voidps.type.Tile

class BeastOfBurdenStoreTest : WorldTest() {

    @Test
    fun `store item into empty beast of burden`() {
        val player = createPlayer(Tile(3200, 3200))
        player.inventory.set(0, "coins", 100)

        val familiar = NPCDefinitions.get("pack_yak_familiar")
        player.summonFamiliar(familiar, false)
        tick(3)

        assertEquals(30, player.beastOfBurdenCapacity)
        assertTrue(player.hasBeastOfBurden())
        assertEquals(30, InventoryDefinitions.get("beast_of_burden").length)
        assertEquals(30, player.beastOfBurden.size)

        player.inventory.transaction {
            val moved = moveToLimit("coins", 10, player.beastOfBurden)
            assertEquals(10, moved)
        }
        assertTrue(player.inventory.transaction.error !is TransactionError.Full)
        assertEquals(10, player.beastOfBurden.count("coins"))
    }
}
