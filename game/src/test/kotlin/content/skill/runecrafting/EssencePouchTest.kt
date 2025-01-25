package content.skill.runecrafting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.itemOnItem
import world.gregs.voidps.world.script.itemOption
import kotlin.test.assertFalse

internal class EssencePouchTest : WorldTest() {

    @Test
    fun `Fill essence with empty pouch`() {
        val player = createPlayer("player")
        player.inventory.add("small_pouch")
        player.inventory.add("rune_essence", 2)
        player.inventory.add("pure_essence", 2)

        player.itemOption("Fill", "small_pouch")

        assertEquals(2, player.inventory.count("rune_essence"))
        assertEquals(0, player.inventory.count("pure_essence"))
        assertEquals(2, player["small_pouch_essence", 0])
        assertTrue(player["small_pouch_pure", false])
    }

    @Test
    fun `Fill essence with partially filled pouch`() {
        val player = createPlayer("player")
        player.inventory.add("small_pouch")
        player["small_pouch_essence"] = 1
        player["small_pouch_pure"] = true
        player.inventory.add("rune_essence")
        player.inventory.add("pure_essence")

        player.itemOption("Fill", "small_pouch")

        assertEquals(1, player.inventory.count("rune_essence"))
        assertEquals(0, player.inventory.count("pure_essence"))
        assertEquals(2, player["small_pouch_essence", 0])
        assertTrue(player["small_pouch_pure", false])
    }

    @Test
    fun `Fill essence with almost full pouch`() {
        val player = createPlayer("player")
        player.inventory.add("small_pouch")
        player["small_pouch_essence"] = 2
        player.inventory.add("rune_essence", 2)

        player.itemOption("Fill", "small_pouch")

        assertEquals(1, player.inventory.count("rune_essence"))
        assertEquals(3, player["small_pouch_essence", 0])
    }

    @Test
    fun `Fill essence with full pouch`() {
        val player = createPlayer("player")
        player.inventory.add("small_pouch")
        player["small_pouch_essence"] = 3
        player.inventory.add("rune_essence", 2)

        player.itemOption("Fill", "small_pouch")

        assertEquals(2, player.inventory.count("rune_essence"))
        assertEquals(3, player["small_pouch_essence", 0])
    }

    @Test
    fun `Empty essence from empty pouch`() {
        val player = createPlayer("player")
        player.inventory.set(0, "medium_pouch", 10)

        player.itemOption("Empty", "medium_pouch")

        assertEquals(10, player.inventory.charges(player, 0))
        assertEquals(0, player.inventory.count("pure_essence"))
        assertEquals(0, player.inventory.count("rune_essence"))
        assertEquals(0, player["small_pouch_essence", 0])
    }

    @Test
    fun `Empty essence from partially filled pouch`() {
        val player = createPlayer("player")
        player.inventory.set(0, "medium_pouch", 10)
        player["medium_pouch_essence"] = 5

        player.itemOption("Empty", "medium_pouch")

        assertEquals(9, player.inventory.charges(player, 0))
        assertEquals(5, player.inventory.count("rune_essence"))
        assertEquals(0, player["small_pouch_essence", 0])
    }

    @Test
    fun `Empty essence from full pouch`() {
        val player = createPlayer("player")
        player.inventory.set(0, "large_pouch", 10)
        player["large_pouch_essence"] = 9
        player["large_pouch_pure"] = true

        player.itemOption("Empty", "large_pouch")

        assertEquals(9, player.inventory.charges(player, 0))
        assertEquals(9, player.inventory.count("pure_essence"))
        assertEquals(0, player.inventory.count("rune_essence"))
        assertEquals(0, player["large_pouch_essence", 0])
    }

    @Test
    fun `Empty essence from full pouch into almost full inventory`() {
        val player = createPlayer("player")
        player.inventory.set(0, "giant_pouch", 10)
        player["giant_pouch_essence"] = 12
        player["giant_pouch_pure"] = true
        player.inventory.add("shark", 22)

        player.itemOption("Empty", "giant_pouch")

        assertEquals(9, player.inventory.charges(player, 0))
        assertEquals(5, player.inventory.count("pure_essence"))
        assertEquals(0, player.inventory.count("rune_essence"))
        assertEquals(7, player["giant_pouch_essence", 0])
    }

    @Test
    fun `Empty essence damages pouch`() {
        val player = createPlayer("player")
        player.inventory.set(0, "medium_pouch", 1)
        player["medium_pouch_essence"] = 6
        player["medium_pouch_pure"] = false

        player.itemOption("Empty", "medium_pouch")

        assertFalse(player.inventory.contains("medium_pouch"))
        assertTrue(player.inventory.contains("medium_pouch_damaged"))
        assertEquals(20, player.inventory.charges(player, 0))
        assertEquals(0, player.inventory.count("pure_essence"))
        assertEquals(6, player.inventory.count("rune_essence"))
        assertEquals(0, player["medium_pouch_essence", 0])
    }

    @Test
    fun `Add essence to empty pouch`() {
        val player = createPlayer("player")
        player.inventory.add("small_pouch")
        player.inventory.add("rune_essence", 2)
        player.inventory.add("pure_essence", 2)

        player.itemOnItem(1, 0)

        assertEquals(1, player.inventory.count("rune_essence"))
        assertEquals(2, player.inventory.count("pure_essence"))
        assertEquals(1, player["small_pouch_essence", 0])
        assertFalse(player["small_pouch_pure", false])
    }

    @Test
    fun `Can't add pure essence to partially filled rune essence pouch`() {
        val player = createPlayer("player")
        player.inventory.add("small_pouch")
        player["medium_pouch_pure"] = false
        player["medium_pouch_essence"] = 2
        player.inventory.add("rune_essence", 2)
        player.inventory.add("pure_essence", 2)

        player.itemOnItem(3, 0)

        assertEquals(2, player.inventory.count("rune_essence"))
        assertEquals(2, player.inventory.count("pure_essence"))
        assertEquals(2, player["medium_pouch_essence", 0])
        assertFalse(player["small_pouch_pure", false])
    }

    @Test
    fun `Can't add rune essence to partially filled pure essence pouch`() {
        val player = createPlayer("player")
        player.inventory.add("small_pouch")
        player["medium_pouch_pure"] = true
        player["medium_pouch_essence"] = 2
        player.inventory.add("rune_essence", 2)
        player.inventory.add("pure_essence", 2)

        player.itemOnItem(3, 0)

        assertEquals(2, player.inventory.count("rune_essence"))
        assertEquals(2, player.inventory.count("pure_essence"))
        assertEquals(2, player["medium_pouch_essence", 0])
        assertTrue(player["medium_pouch_pure", false])
    }

    @Test
    fun `Dropping a pouch removes the essence inside`() {
        val player = createPlayer("player")
        player.inventory.set(0, "medium_pouch", 10)
        player["medium_pouch_essence"] = 2

        player.itemOption("Drop", "medium_pouch")

        assertFalse(player.inventory.contains("medium_pouch"))
        assertEquals(0, player.inventory.count("pure_essence"))
        assertEquals(0, player.inventory.count("rune_essence"))
        assertEquals(0, player["medium_pouch_essence", 0])
    }

}