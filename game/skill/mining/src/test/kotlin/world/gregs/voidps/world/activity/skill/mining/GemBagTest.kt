package world.gregs.voidps.world.activity.skill.mining

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.dialogueOption
import world.gregs.voidps.world.script.itemOnItem
import world.gregs.voidps.world.script.itemOption

internal class GemBagTest : WorldTest() {

    @Test
    fun `Add gems to empty bag`() {
        val player = createPlayer("player")
        player.inventory.add("gem_bag")
        player.inventory.add("uncut_emerald", 5)
        player.inventory.add("uncut_diamond", 4)

        player.itemOnItem(0, 1)

        assertEquals(0, player.inventory.count("uncut_emerald"))
        assertEquals(4, player.inventory.count("uncut_diamond"))
        assertEquals(5, player["gem_bag_emerald", 0])
    }

    @Test
    fun `Add gems to partially filled bag`() {
        val player = createPlayer("player")
        player.inventory.add("gem_bag")
        player["gem_bag_ruby"] = 10
        player.inventory.add("uncut_ruby", 5)

        player.itemOnItem(0, 1)

        assertEquals(0, player.inventory.count("uncut_ruby"))
        assertEquals(15, player["gem_bag_ruby", 0])
    }

    @Test
    fun `Add gems to almost full bag`() {
        val player = createPlayer("player")
        player.inventory.add("gem_bag")
        player["gem_bag_sapphire"] = 10
        player["gem_bag_emerald"] = 10
        player["gem_bag_ruby"] = 70
        player["gem_bag_diamond"] = 8
        player.inventory.add("uncut_diamond", 5)

        player.itemOnItem(1, 0)

        assertEquals(3, player.inventory.count("uncut_diamond"))
        assertEquals(10, player["gem_bag_diamond", 0])
    }

    @Test
    fun `Add gems to full bag`() {
        val player = createPlayer("player")
        player.inventory.add("gem_bag")
        player["gem_bag_sapphire"] = 25
        player["gem_bag_emerald"] = 25
        player["gem_bag_ruby"] = 25
        player["gem_bag_diamond"] = 25
        player.inventory.add("uncut_sapphire", 5)

        player.itemOnItem(1, 0)

        assertEquals(5, player.inventory.count("uncut_sapphire"))
        assertEquals(25, player["gem_bag_sapphire", 0])
    }

    @Test
    fun `Withdraw gem from bag to almost full inventory`() {
        val player = createPlayer("player")
        player.inventory.add("gem_bag")
        player["gem_bag_sapphire"] = 25
        player["gem_bag_emerald"] = 25
        player["gem_bag_ruby"] = 25
        player["gem_bag_diamond"] = 25
        player.inventory.add("shark", 20)

        player.itemOption("Withdraw", "gem_bag")

        assertEquals(7, player.inventory.count("uncut_sapphire"))
        assertEquals(18, player["gem_bag_sapphire", 0])
    }

    @Test
    fun `Withdraw gem from bag to full inventory`() {
        val player = createPlayer("player")
        player.inventory.add("gem_bag")
        player["gem_bag_emerald"] = 25
        player.inventory.add("shark", 27)

        player.itemOption("Withdraw", "gem_bag")

        assertEquals(0, player.inventory.count("emerald"))
        assertEquals(25, player["gem_bag_emerald", 0])
    }

    @Test
    fun `Can't destroy filled gem bag`() {
        val player = createPlayer("player")
        player["gem_bag_emerald"] = 25
        player.inventory.add("gem_bag")

        player.itemOption("Destroy", "gem_bag")
        tick()
        player.dialogueOption("confirm")

        assertTrue(player.inventory.contains("gem_bag"))
        assertEquals(25, player["gem_bag_emerald", 0])
    }

    @Test
    fun `Destroy empty gem bag`() {
        val player = createPlayer("player")
        player.inventory.add("gem_bag")

        player.itemOption("Destroy", "gem_bag")
        tick()
        player.dialogueOption("confirm")

        assertFalse(player.inventory.contains("gem_bag"))
    }

}