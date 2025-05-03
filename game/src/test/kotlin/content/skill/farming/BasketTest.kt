package content.skill.farming

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import WorldTest
import itemOnItem
import itemOption
import kotlin.test.assertFalse

internal class BasketTest : WorldTest() {

    @Test
    fun `Fill empty sack with multiple vegetables`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("banana")
        player.inventory.add("strawberry", 5)
        player.inventory.add("basket")

        player.itemOption("Fill", "basket")

        assertTrue(player.inventory.contains("strawberries_5"))
        assertFalse(player.inventory.contains("strawberry"))
        assertTrue(player.inventory.contains("banana"))
    }

    @Test
    fun `Fill part filled sack with multiple vegetables`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("banana", 5)
        player.inventory.add("bananas_2")

        player.itemOption("Fill", "bananas_2")

        assertTrue(player.inventory.contains("bananas_5"))
        assertEquals(2, player.inventory.count("banana"))
    }

    @Test
    fun `Fill almost full sack with multiple vegetables`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("strawberry", 5)
        player.inventory.add("strawberries_2")

        player.itemOption("Fill", "strawberries_2")

        println(player.inventory.items.toList())
        assertTrue(player.inventory.contains("strawberries_5"))
        assertEquals(2, player.inventory.count("strawberry"))
    }

    @Test
    fun `Sack can't be filled with more vegetables when full`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("cooking_apple", 5)
        player.inventory.add("apples_5")

        player.itemOption("Fill", "apples_5")

        assertTrue(player.inventory.contains("apples_5"))
        assertEquals(5, player.inventory.count("cooking_apple"))
    }

    @Test
    fun `Add one vegetable to empty sack`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("strawberry", 2)
        player.inventory.add("basket")

        player.itemOnItem(0, 2)
        tick(2)

        assertTrue(player.inventory.contains("strawberries_1"))
        assertEquals(1, player.inventory.count("strawberry"))
    }

    @Test
    fun `Add one vegetable to partially filled sack`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("banana", 2)
        player.inventory.add("bananas_4")

        player.itemOnItem(0, 2)
        tick(2)

        assertTrue(player.inventory.contains("bananas_5"))
        assertEquals(1, player.inventory.count("banana"))

    }

    @Test
    fun `Can't add one vegetable to full sack`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("cooking_apple", 2)
        player.inventory.add("apples_5")

        player.itemOnItem(0, 2)
        tick(2)

        assertTrue(player.inventory.contains("apples_5"))
        assertEquals(2, player.inventory.count("cooking_apple"))
    }

    @Test
    fun `Empty partially filled sack`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("bananas_3")

        player.itemOption("Empty", "bananas_3")

        assertTrue(player.inventory.contains("basket"))
        assertEquals(3, player.inventory.count("banana"))
    }

    @Test
    fun `Empty partially filled sack into almost full inventory`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("apples_4")
        player.inventory.add("shark", 25)

        player.itemOption("Empty", "apples_4")

        assertTrue(player.inventory.contains("apples_2"))
        assertEquals(2, player.inventory.count("cooking_apple"))
    }

    @Test
    fun `Can't empty partially filled sack into full inventory`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("strawberries_4")
        player.inventory.add("shark", 27)

        player.itemOption("Empty", "strawberries_4")

        assertTrue(player.inventory.contains("strawberries_4"))
        assertEquals(0, player.inventory.count("strawberry"))
    }

    @Test
    fun `Remove one vegetable from full sack`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("apples_5")

        player.itemOption("Remove-one", "apples_5")

        assertTrue(player.inventory.contains("apples_4"))
        assertEquals(1, player.inventory.count("cooking_apple"))
    }

    @Test
    fun `Remove one vegetable from full sack into almost full inventory`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("bananas_5")
        player.inventory.add("shark", 26)

        player.itemOption("Remove-one", "bananas_5")

        assertTrue(player.inventory.contains("bananas_4"))
        assertEquals(1, player.inventory.count("banana"))
    }

    @Test
    fun `Can't remove one vegetable from full sack into full inventory`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("strawberries_5")
        player.inventory.add("shark", 27)

        player.itemOption("Remove-one", "strawberries_5")

        assertTrue(player.inventory.contains("strawberries_5"))
        assertEquals(0, player.inventory.count("strawberry"))
    }

}