package content.area.tirannwn

import WorldTest
import containsMessage
import dialogueContinue
import dialogueOption
import itemOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class LletyaTest : WorldTest() {

    @Test
    fun `Activate teleport crystal to teleport to lletya`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("crystal_teleport_seed_4")

        player.itemOption("Activate", "crystal_teleport_seed_4")
        tick()
        player.dialogueOption(1) // "Teleport to Lletya."
        tickIf { player.tile.x !in 2328..2330 }
        tick(2)

        assertTrue(player.tile.x in 2328..2330 && player.tile.y in 3171..3173)
        assertEquals(1, player.inventory.count("crystal_teleport_seed_3"))
        assertTrue(player.containsMessage("Your teleportation crystal has degraded from use."))
    }

    @Test
    fun `Eluned recharges uncharged teleport crystals with a sliding price`() {
        val player = createPlayer(emptyTile)
        val eluned = createNPC("eluned", emptyTile.addX(1))
        player.inventory.add("crystal_teleport_seed_uncharged")
        player.inventory.add("coins", 750)

        player.npcOption(eluned, "Talk-to")
        tick(2)
        player.dialogueContinue(3) // greeting, request, price quote
        player.dialogueOption(1) // "Recharge a crystal."
        player.dialogueContinue() // repeated player line
        tick(2)

        assertEquals(1, player.inventory.count("crystal_teleport_seed_4"))
        assertEquals(0, player.inventory.count("coins"))
        assertEquals(1, player["teleport_crystal_recharges", 0])
    }

    @Test
    fun `Eluned recharge is cheaper after the first`() {
        val player = createPlayer(emptyTile)
        val eluned = createNPC("eluned", emptyTile.addX(1))
        player["teleport_crystal_recharges"] = 1
        player.inventory.add("crystal_teleport_seed_uncharged")
        player.inventory.add("coins", 600)

        player.npcOption(eluned, "Talk-to")
        tick(2)
        player.dialogueContinue(3)
        player.dialogueOption(1)
        player.dialogueContinue()
        tick(2)

        assertEquals(1, player.inventory.count("crystal_teleport_seed_4"))
        assertEquals(0, player.inventory.count("coins"))
        assertEquals(2, player["teleport_crystal_recharges", 0])
    }

    @Test
    fun `Islwyn attunes a crystal seed into a bow`() {
        val player = createPlayer(emptyTile)
        player["roving_elves"] = "completed"
        val islwyn = createNPC("islwyn", emptyTile.addX(1))
        player.inventory.add("crystal_seed")
        player.inventory.add("coins", 900_000)

        player.npcOption(islwyn, "Talk-to")
        tick(2)
        player.dialogueContinue() // greeting
        player.dialogueOption(2) // "I need to recharge my seeds into equipment."
        player.dialogueContinue(2) // repeated player line, price quote
        player.dialogueOption(1) // "Recharge my seed into a bow, please."
        player.dialogueContinue() // repeated player line
        tick(2)

        assertEquals(1, player.inventory.count("crystal_bow_full"))
        assertEquals(0, player.inventory.count("crystal_seed"))
        assertEquals(0, player.inventory.count("coins"))
        assertEquals(1, player["crystal_seed_attunements", 0])
    }

    @Test
    fun `Islwyn won't attune a seed without enough coins`() {
        val player = createPlayer(emptyTile)
        player["roving_elves"] = "completed"
        val islwyn = createNPC("islwyn", emptyTile.addX(1))
        player.inventory.add("crystal_seed")

        player.npcOption(islwyn, "Talk-to")
        tick(2)
        player.dialogueContinue()
        player.dialogueOption(2)
        player.dialogueContinue(2)
        player.dialogueOption(1)
        player.dialogueContinue()
        tick(2)

        assertEquals(1, player.inventory.count("crystal_seed"))
        assertEquals(0, player.inventory.count("crystal_bow_full"))
    }

    @Test
    fun `Lletya shopkeeper dialogue opens the shop`() {
        val player = createPlayer(emptyTile)
        val eudav = createNPC("eudav", emptyTile.addX(1))

        player.npcOption(eudav, "Talk-to")
        tick(2)
        player.dialogueContinue() // "Can I help you at all?"
        player.dialogueOption(1) // "Yes please. What are you selling?"
        player.dialogueContinue() // repeated player line
        tick(2)

        assertTrue(player.hasOpen("shop"))
    }
}
