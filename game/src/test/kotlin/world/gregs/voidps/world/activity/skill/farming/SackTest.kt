package world.gregs.voidps.world.activity.skill.farming

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.itemOnItem
import world.gregs.voidps.world.script.itemOption
import kotlin.test.assertFalse

internal class SackTest : WorldTest() {

    @Test
    fun `Fill empty sack with multiple vegetables`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("onion")
        player.inventory.add("raw_potato", 5)
        player.inventory.add("empty_sack")

        player.itemOption("Fill", "empty_sack")

        assertTrue(player.inventory.contains("potatoes_5"))
        assertFalse(player.inventory.contains("raw_potato"))
        assertTrue(player.inventory.contains("onion"))
    }

    @Test
    fun `Fill part filled sack with multiple vegetables`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("onion", 5)
        player.inventory.add("onions_2")

        player.itemOption("Fill", "onions_2")

        assertTrue(player.inventory.contains("onions_7"))
        assertFalse(player.inventory.contains("onion"))
    }

    @Test
    fun `Fill almost full sack with multiple vegetables`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("raw_potato", 5)
        player.inventory.add("potatoes_7")

        player.itemOption("Fill", "potatoes_7")

        assertTrue(player.inventory.contains("potatoes_10"))
        assertEquals(2, player.inventory.count("raw_potato"))
    }

    @Test
    fun `Sack can't be filled with more vegetables when full`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("cabbage", 5)
        player.inventory.add("cabbages_10")

        player.itemOption("Fill", "cabbages_10")

        assertTrue(player.inventory.contains("cabbages_10"))
        assertEquals(5, player.inventory.count("cabbage"))
    }

    @Test
    fun `Add one vegetable to empty sack`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("raw_potato", 2)
        player.inventory.add("empty_sack")

        player.itemOnItem(0, 2)
        tick(2)

        assertTrue(player.inventory.contains("potatoes_1"))
        assertEquals(1, player.inventory.count("raw_potato"))
    }

    @Test
    fun `Add one vegetable to partially filled sack`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("onion", 2)
        player.inventory.add("onions_9")

        player.itemOnItem(0, 2)
        tick(2)

        assertTrue(player.inventory.contains("onions_10"))
        assertEquals(1, player.inventory.count("onion"))

    }

    @Test
    fun `Can't add one vegetable to full sack`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("cabbage", 2)
        player.inventory.add("cabbages_10")

        player.itemOnItem(0, 2)
        tick(2)

        assertTrue(player.inventory.contains("cabbages_10"))
        assertEquals(2, player.inventory.count("cabbage"))
    }

    @Test
    fun `Empty partially filled sack`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("onions_6")

        player.itemOption("Empty", "onions_6")

        assertTrue(player.inventory.contains("empty_sack"))
        assertEquals(6, player.inventory.count("onion"))
    }

    @Test
    fun `Empty partially filled sack into almost full inventory`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("cabbages_6")
        player.inventory.add("orange", 22)

        player.itemOption("Empty", "cabbages_6")

        assertTrue(player.inventory.contains("cabbages_1"))
        assertEquals(5, player.inventory.count("cabbage"))
    }

    @Test
    fun `Can't empty partially filled sack into full inventory`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("potatoes_6")
        player.inventory.add("orange", 27)

        player.itemOption("Empty", "potatoes_6")

        assertTrue(player.inventory.contains("potatoes_6"))
        assertEquals(0, player.inventory.count("raw_potato"))
    }

    @Test
    fun `Remove one vegetable from full sack`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("cabbages_10")

        player.itemOption("Remove-one", "cabbages_10")

        assertTrue(player.inventory.contains("cabbages_9"))
        assertEquals(1, player.inventory.count("cabbage"))
    }

    @Test
    fun `Remove one vegetable from full sack into almost full inventory`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("onions_6")
        player.inventory.add("orange", 26)

        player.itemOption("Remove-one", "onions_6")

        assertTrue(player.inventory.contains("onions_5"))
        assertEquals(1, player.inventory.count("onion"))
    }

    @Test
    fun `Can't remove one vegetable from full sack into full inventory`() {
        val player = createPlayer("player", emptyTile)
        player.inventory.add("potatoes_6")
        player.inventory.add("orange", 27)

        player.itemOption("Remove-one", "potatoes_6")

        assertTrue(player.inventory.contains("potatoes_6"))
        assertEquals(0, player.inventory.count("raw_potato"))
    }

}