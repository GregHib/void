package content.area.kharidian_desert.magic_carpet

import WorldTest
import dialogueContinue
import dialogueContinues
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class RugMerchantTest : WorldTest() {

    @Test
    fun `Can't travel without coins`() {
        val player = createPlayer(Tile(3309, 3109))
        val merchant = createNPC("rug_merchant_shantay_pass", Tile(3310, 3109))

        player.npcOption(merchant, "Talk-to")
        tick()
        player.dialogueContinues()
        player.dialogueOption("line1")
        player.dialogueOption("line1")

        tick(10)
        assertEquals(Tile(3309, 3109), player.tile)
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Travel is cheaper using Ring of Charos`() {
        val player = createPlayer(Tile(3309, 3109))
        player.inventory.add("coins", 200)
        player.equipment.set(EquipSlot.Ring.index, "ring_of_charos_a")
        val merchant = createNPC("rug_merchant_shantay_pass", Tile(3310, 3109))

        player.npcOption(merchant, "Talk-to")
        tick()
        player.dialogueContinues()
        player.dialogueOption("line1") // Would you like to travel: Yes please.
        player.dialogueContinues()
        player.dialogueOption("line1") // I want to travel to
        player.dialogueContinues()
        player.dialogueOption("line2") // How about 100gp?
        player.dialogueContinues()

        tick(10)
        assertNotEquals(Tile(3309, 3109), player.tile)
        assertEquals(100, player.inventory.count("coins"))
    }

    @Test
    fun `Travel is cheaper after Rouge Trader quest`() {
        val player = createPlayer(Tile(3309, 3109))
        player["rogue_trader"] = "completed"
        player.inventory.add("coins", 200)
        val merchant = createNPC("rug_merchant_shantay_pass", Tile(3310, 3109))

        player.npcOption(merchant, "Talk-to")
        tick()
        player.dialogueContinues()
        player.dialogueOption("line1") // Would you like to travel: Yes please.
        player.dialogueContinues()
        player.dialogueOption("line1") // I want to travel to
        player.dialogueContinues()

        tick(10)
        assertNotEquals(Tile(3309, 3109), player.tile)
        assertEquals(100, player.inventory.count("coins"))
    }

    @Test
    fun `Travel is cheapest with Rouge Trader and Ring of Charos`() {
        val player = createPlayer(Tile(3309, 3109))
        player.inventory.add("coins", 200)
        player["rogue_trader"] = "completed"
        player.equipment.set(EquipSlot.Ring.index, "ring_of_charos_a")
        val merchant = createNPC("rug_merchant_shantay_pass", Tile(3310, 3109))

        player.npcOption(merchant, "Talk-to")
        tick()
        player.dialogueContinues()
        player.dialogueOption("line1") // Would you like to travel: Yes please.
        player.dialogueContinues()
        player.dialogueOption("line1") // I want to travel to
        player.dialogueContinues()
        player.dialogueOption("line2") // How about 75gp?
        player.dialogueContinues()

        tick(10)
        assertNotEquals(Tile(3309, 3109), player.tile)
        assertEquals(125, player.inventory.count("coins"))
    }

}