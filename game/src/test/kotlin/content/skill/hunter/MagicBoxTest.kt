package content.skill.hunter

import FakeRandom
import WorldTest
import content.entity.player.bank.bank
import interfaceOption
import itemOnItem
import itemOption
import objectOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MagicBoxTest : WorldTest() {
    @Test
    fun `Catch an imp`() {
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("magic_box")
        player.levels.set(Skill.Hunter, 99)

        player.itemOption("Activate", "magic_box")
        tick(3)
        assertTrue(GameObjects.at(start).any { it.id == "magic_box" })
        createNPC("imp", player.tile.addY(2))

        tick(22)

        val trap = GameObjects.at(start).firstOrNull { it.id == "magic_box_caught" }
        assertNotNull(trap)

        player.objectOption(trap, "Retrieve")
        tick(3)
        assertEquals(1, player.inventory.count("imp_in_a_box_2"))
        assertEquals(0, player.inventory.count("magic_box"))
        assertEquals(450.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Can't lay without hunter level`() {
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("magic_box")
        player.levels.set(Skill.Hunter, 70)

        player.itemOption("Activate", "magic_box")
        tick(3)
        assertFalse(GameObjects.at(start).any { it.id == "magic_box" })
        assertEquals(1, player.inventory.count("magic_box"))
    }

    @Test
    fun `Fail to catch`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 4) 0 else until - 1
        })
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("magic_box")
        player.levels.set(Skill.Hunter, 71)

        player.itemOption("Activate", "magic_box")
        tick(3)
        assertTrue(GameObjects.at(start).any { it.id == "magic_box" })
        createNPC("imp", player.tile.addY(2))

        tick(22)

        val trap = GameObjects.at(start).firstOrNull { it.id == "magic_box_fail" }
        assertNotNull(trap)

        player.objectOption(trap, "Deactivate")
        tick(3)
        assertEquals(1, player.inventory.count("magic_box"))
        assertEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Imp in a box banks items with two charges`() {
        val player = createPlayer()
        player.inventory.add("imp_in_a_box_2")
        player.inventory.add("bones", 2)

        player.itemOnItem("bones", "imp_in_a_box_2")
        tick(2)
        assertEquals(1, player.inventory.count("imp_in_a_box_1"))
        assertEquals(0, player.inventory.count("imp_in_a_box_2"))
        assertEquals(1, player.inventory.count("bones"))
        assertEquals(1, player.bank.count("bones"))

        player.itemOnItem("bones", "imp_in_a_box_1")
        tick(2)
        assertEquals(0, player.inventory.count("imp_in_a_box_1"))
        assertEquals(1, player.inventory.count("magic_box"))
        assertEquals(0, player.inventory.count("bones"))
        assertEquals(2, player.bank.count("bones"))
    }

    @Test
    fun `Bank option opens the imp release negotiation form`() {
        val player = createPlayer()
        player.inventory.add("imp_in_a_box_2")
        player.inventory.add("bones", 2)

        player.itemOption("Bank", "imp_in_a_box_2")
        tick(2)
        assertTrue(player.hasOpen("imp_box"))

        player.interfaceOption("imp_box", "inventory", "Deposit", item = Item("bones"), slot = player.inventory.indexOf("bones"))
        tick(2)
        assertEquals(1, player.bank.count("bones"))
        assertEquals(1, player.inventory.count("imp_in_a_box_1"))
        assertTrue(player.hasOpen("imp_box"))

        player.interfaceOption("imp_box", "inventory", "Deposit", item = Item("bones"), slot = player.inventory.indexOf("bones"))
        tick(2)
        assertEquals(2, player.bank.count("bones"))
        assertEquals(1, player.inventory.count("magic_box"))
        assertFalse(player.hasOpen("imp_box"))
    }

    @Test
    fun `Imp box can't be deposited through the form`() {
        val player = createPlayer()
        player.inventory.add("imp_in_a_box_2")
        player.inventory.add("imp_in_a_box_1")

        player.itemOption("Bank", "imp_in_a_box_2")
        tick(2)
        assertTrue(player.hasOpen("imp_box"))

        player.interfaceOption("imp_box", "inventory", "Deposit", item = Item("imp_in_a_box_1"), slot = player.inventory.indexOf("imp_in_a_box_1"))
        tick(2)
        assertEquals(0, player.bank.count("imp_in_a_box_1"))
        assertEquals(1, player.inventory.count("imp_in_a_box_2"))
    }

    @Test
    fun `Imp refuses to bank another imp box`() {
        val player = createPlayer()
        player.inventory.add("imp_in_a_box_2")
        player.inventory.add("imp_in_a_box_1")

        player.itemOnItem("imp_in_a_box_1", "imp_in_a_box_2")
        tick(2)
        assertEquals(1, player.inventory.count("imp_in_a_box_2"))
        assertEquals(1, player.inventory.count("imp_in_a_box_1"))
        assertEquals(0, player.bank.count("imp_in_a_box_1"))
    }
}
