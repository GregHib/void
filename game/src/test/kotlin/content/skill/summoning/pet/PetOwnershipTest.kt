package content.skill.summoning.pet

import WorldTest
import containsMessage
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.inventory

internal class PetOwnershipTest : WorldTest() {

    @Test
    fun `another player cannot pick up your pet`() {
        val owner = createPlayer(emptyTile)
        val petNpc = createNPC("pet_cat_baby", emptyTile)
        owner.pet = petNpc
        owner.set("pet_active_item", "pet_kitten")

        val intruder = createPlayer(emptyTile)
        // Give the intruder their own pet slot to prove the index-mismatch
        // branch (not the null-pet branch) blocks the pickup.
        val intruderPet = createNPC("pet_cat_baby", emptyTile)
        intruder.pet = intruderPet
        intruder.set("pet_active_item", "pet_kitten")

        intruder.npcOption(petNpc, "Pick-up")
        tick(3)

        // Pet still belongs to its owner.
        assertEquals(petNpc.index, owner.get("pet_index", -1))
        assertEquals("pet_kitten", owner.get("pet_active_item", ""))
        // Intruder gained no kitten item and saw the guard message.
        assertFalse(intruder.inventory.contains("pet_kitten"))
        assertTrue(intruder.containsMessage("This isn't your pet."))
    }

    @Test
    fun `non-owner without their own pet still cannot pick up someone elses`() {
        val owner = createPlayer(emptyTile)
        val petNpc = createNPC("pet_cat_baby", emptyTile)
        owner.pet = petNpc
        owner.set("pet_active_item", "pet_kitten")

        val intruder = createPlayer(emptyTile)
        // Intruder has no pet slot; the owner == null branch is the one we hit.
        intruder.npcOption(petNpc, "Pick-up")
        tick(3)

        assertEquals(-1, intruder.get("pet_index", -1))
        assertEquals(petNpc.index, owner.get("pet_index", -1))
        assertTrue(intruder.containsMessage("This isn't your pet."))
    }
}
