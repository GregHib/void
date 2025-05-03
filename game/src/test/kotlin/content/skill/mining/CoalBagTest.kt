package content.skill.mining

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import WorldTest
import dialogueOption
import itemOnItem
import itemOption

internal class CoalBagTest : WorldTest() {

    @Test
    fun `Add coal to empty bag`() {
        val player = createPlayer()
        player.inventory.add("coal_bag")
        player.inventory.add("coal", 5)

        player.itemOnItem(0, 1)

        assertEquals(0, player.inventory.count("coal"))
        assertEquals(5, player["coal_bag_coal", 0])
    }

    @Test
    fun `Add coal to partially filled bag`() {
        val player = createPlayer()
        player.inventory.add("coal_bag")
        player["coal_bag_coal"] = 10
        player.inventory.add("coal", 5)

        player.itemOnItem(0, 1)

        assertEquals(0, player.inventory.count("coal"))
        assertEquals(15, player["coal_bag_coal", 0])
    }

    @Test
    fun `Add coal to almost full bag`() {
        val player = createPlayer()
        player.inventory.add("coal_bag")
        player["coal_bag_coal"] = 75
        player.inventory.add("coal", 10)

        player.itemOnItem(0, 1)

        assertEquals(4, player.inventory.count("coal"))
        assertEquals(81, player["coal_bag_coal", 0])
    }

    @Test
    fun `Add coal to full bag`() {
        val player = createPlayer()
        player.inventory.add("coal_bag")
        player["coal_bag_coal"] = 81
        player.inventory.add("coal", 5)

        player.itemOnItem(0, 1)

        assertEquals(5, player.inventory.count("coal"))
        assertEquals(81, player["coal_bag_coal", 0])
    }

    @Test
    fun `Withdraw one coal from bag to almost full inventory`() {
        val player = createPlayer()
        player.inventory.add("coal_bag")
        player["coal_bag_coal"] = 10
        player.inventory.add("shark", 26)

        player.itemOption("Withdraw-one", "coal_bag")

        assertEquals(1, player.inventory.count("coal"))
        assertEquals(9, player["coal_bag_coal", 0])
    }

    @Test
    fun `Withdraw one coal from bag to full inventory`() {
        val player = createPlayer()
        player.inventory.add("coal_bag")
        player["coal_bag_coal"] = 10
        player.inventory.add("shark", 27)

        player.itemOption("Withdraw-one", "coal_bag")

        assertEquals(0, player.inventory.count("coal"))
        assertEquals(10, player["coal_bag_coal", 0])
    }

    @Test
    fun `Withdraw many coal from bag`() {
        val player = createPlayer()
        player.inventory.add("coal_bag")
        player["coal_bag_coal"] = 10

        player.itemOption("Withdraw-many", "coal_bag")

        assertEquals(10, player.inventory.count("coal"))
        assertEquals(0, player["coal_bag_coal", 0])
    }

    @Test
    fun `Withdraw many coal from bag to almost full inventory`() {
        val player = createPlayer()
        player.inventory.add("coal_bag")
        player["coal_bag_coal"] = 10
        player.inventory.add("shark", 24)

        player.itemOption("Withdraw-many", "coal_bag")

        assertEquals(3, player.inventory.count("coal"))
        assertEquals(7, player["coal_bag_coal", 0])
    }

    @Test
    fun `Withdraw many coal from bag to full inventory`() {
        val player = createPlayer()
        player.inventory.add("coal_bag")
        player["coal_bag_coal"] = 10
        player.inventory.add("shark", 27)

        player.itemOption("Withdraw-many", "coal_bag")

        assertEquals(0, player.inventory.count("coal"))
        assertEquals(10, player["coal_bag_coal", 0])
    }

    @Test
    fun `Can't destroy filled gem bag`() {
        val player = createPlayer()
        player["coal_bag_coal"] = 25
        player.inventory.add("coal_bag")

        player.itemOption("Destroy", "coal_bag")
        tick()
        player.dialogueOption("confirm")

        assertTrue(player.inventory.contains("coal_bag"))
        assertEquals(25, player["coal_bag_coal", 0])
    }

    @Test
    fun `Destroy empty gem bag`() {
        val player = createPlayer()
        player.inventory.add("coal_bag")

        player.itemOption("Destroy", "coal_bag")
        tick()
        player.dialogueOption("confirm")

        assertFalse(player.inventory.contains("coal_bag"))
    }

}