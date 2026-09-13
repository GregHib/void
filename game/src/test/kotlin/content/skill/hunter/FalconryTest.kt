package content.skill.hunter

import FakeRandom
import WorldTest
import npcOption
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FalconryTest : WorldTest() {
    @Test
    fun `Borrow a falcon from Matthias`() {
        val player = createPlayer(Tile(2376, 3605))
        val matthias = createNPC("matthias", Tile(2376, 3606))
        player.inventory.add("coins", 500)
        player.levels.set(Skill.Hunter, 43)

        player.npcOption(matthias, "Falconry")
        tick(5)
        assertEquals("falconers_glove_2", player.equipped(EquipSlot.Weapon).id)
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Can't borrow a falcon without the level`() {
        val player = createPlayer(Tile(2376, 3605))
        val matthias = createNPC("matthias", Tile(2376, 3606))
        player.inventory.add("coins", 500)

        player.npcOption(matthias, "Falconry")
        tick(5)
        assertTrue(player.equipped(EquipSlot.Weapon).isEmpty())
        assertEquals(500, player.inventory.count("coins"))
    }

    @Test
    fun `Can't borrow a falcon without coins`() {
        val player = createPlayer(Tile(2376, 3605))
        val matthias = createNPC("matthias", Tile(2376, 3606))
        player.levels.set(Skill.Hunter, 43)

        player.npcOption(matthias, "Falconry")
        tick(5)
        assertTrue(player.equipped(EquipSlot.Weapon).isEmpty())
    }

    @Test
    fun `Catch and retrieve a kebbit`() {
        val player = createPlayer(Tile(2376, 3605))
        val matthias = createNPC("matthias", Tile(2376, 3606))
        player.inventory.add("coins", 500)
        player.levels.set(Skill.Hunter, 99)
        player.npcOption(matthias, "Falconry")
        tick(5)
        assertEquals("falconers_glove_2", player.equipped(EquipSlot.Weapon).id)

        val kebbit = createNPC("spotted_kebbit", Tile(2378, 3600))
        player.npcOption(kebbit, "Catch")
        tick(10)

        assertEquals("falconers_glove", player.equipped(EquipSlot.Weapon).id)
        val caught = NPCs.at(kebbit.tile).firstOrNull { it.id == "spotted_kebbit_caught" }
        assertNotNull(caught)

        player.npcOption(caught, "Retrieve")
        tick(10)
        assertEquals("falconers_glove_2", player.equipped(EquipSlot.Weapon).id)
        assertEquals(1, player.inventory.count("spotted_kebbit_fur"))
        assertEquals(1, player.inventory.count("bones"))
        assertEquals(104.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Fail to catch a kebbit`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 4) 0 else until - 1
        })
        val player = createPlayer(Tile(2376, 3605))
        val matthias = createNPC("matthias", Tile(2376, 3606))
        player.inventory.add("coins", 500)
        player.levels.set(Skill.Hunter, 50)
        player.npcOption(matthias, "Falconry")
        tick(5)

        val kebbit = createNPC("spotted_kebbit", Tile(2378, 3600))
        player.npcOption(kebbit, "Catch")
        tick(10)

        assertEquals("falconers_glove_2", player.equipped(EquipSlot.Weapon).id)
        assertNull(NPCs.at(kebbit.tile).firstOrNull { it.id == "spotted_kebbit_caught" })
        assertEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Leaving the area returns the falcon`() {
        val player = createPlayer(Tile(2376, 3605))
        val matthias = createNPC("matthias", Tile(2376, 3606))
        player.inventory.add("coins", 500)
        player.levels.set(Skill.Hunter, 43)
        player.npcOption(matthias, "Falconry")
        tick(5)
        assertEquals("falconers_glove_2", player.equipped(EquipSlot.Weapon).id)

        player.tele(2340, 3605)
        tick(2)
        assertTrue(player.equipped(EquipSlot.Weapon).isEmpty())
    }
}
