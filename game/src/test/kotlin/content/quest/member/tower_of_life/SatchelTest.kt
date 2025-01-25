package content.quest.member.tower_of_life

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.itemOnItem
import world.gregs.voidps.world.script.itemOption
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class SatchelTest : WorldTest() {

    @Test
    fun `Empty satchel of items`() {
        val player = createPlayer("player")
        player.inventory.set(0, "plain_satchel", 7)

        player.itemOption("Empty", "plain_satchel")

        assertEquals(0, player.inventory.charges(player, 0))
        assertTrue(player.inventory.contains("cake"))
        assertTrue(player.inventory.contains("banana"))
        assertTrue(player.inventory.contains("triangle_sandwich"))
    }

    @Test
    fun `Empty satchel of items into almost full inventory`() {
        val player = createPlayer("player")
        player.inventory.set(0, "plain_satchel", 7)
        player.inventory.add("shark", 26)

        player.itemOption("Empty", "plain_satchel")

        assertEquals(5, player.inventory.charges(player, 0))
        assertTrue(player.inventory.contains("banana"))
        assertFalse(player.inventory.contains("cake"))
        assertFalse(player.inventory.contains("triangle_sandwich"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["cake", "banana", "triangle_sandwich"])
    fun `Add item to food satchel`(item: String) {
        val player = createPlayer("player")
        player.inventory.set(0, "plain_satchel", 0)
        player.inventory.add(item)

        player.itemOnItem(1, 0)

        assertNotEquals(0, player.inventory.charges(player, 0))
        assertFalse(player.inventory.contains(item))
    }

    @ParameterizedTest
    @ValueSource(strings = ["cake", "banana", "triangle_sandwich"])
    fun `Can't add item to full satchel`(item: String) {
        val player = createPlayer("player")
        player.inventory.set(0, "plain_satchel", 7)
        player.inventory.add(item)

        player.itemOnItem(1, 0)

        assertEquals(7, player.inventory.charges(player, 0))
        assertTrue(player.inventory.contains(item))
    }
}