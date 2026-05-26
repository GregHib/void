package content.skill.summoning.pet

import WorldTest
import content.skill.summoning.follower
import itemOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

internal class PetExclusivityTest : WorldTest() {

    @Test
    fun `summonPet refuses when a familiar is already following`() {
        val player = createPlayer(emptyTile)
        // Simulate an active familiar by populating the slot with any NPC.
        val familiar = createNPC("spirit_wolf_familiar", emptyTile)
        player.follower = familiar
        player.inventory.add("pet_kitten")

        player.itemOption("Drop", "pet_kitten")
        tick(3)

        assertTrue(player.inventory.contains("pet_kitten"), "kitten item must stay in inventory")
        assertNull(player.pet, "pet slot must remain empty")
    }

    @Test
    fun `summonFamiliar refuses when a pet is already following`() {
        val player = createPlayer(emptyTile)
        val petNpc = createNPC("pet_cat_baby", emptyTile)
        player.pet = petNpc
        // The Player.pet getter cross-checks pet_active_item to reject
        // stale slot lookups after NPC index reuse; mirror real summon
        // state so the guard treats this player as actually pet-following.
        player.set("pet_active_item", "pet_kitten")
        player.levels.set(Skill.Summoning, 99)
        player.inventory.add("spirit_wolf_pouch")

        player.itemOption("Summon", "spirit_wolf_pouch")
        tick(3)

        assertTrue(player.inventory.contains("spirit_wolf_pouch"), "pouch must stay in inventory")
        assertNull(player.follower, "follower slot must remain empty")
        assertFalse(player.inventory.isEmpty(), "no items should have been consumed")
    }
}
