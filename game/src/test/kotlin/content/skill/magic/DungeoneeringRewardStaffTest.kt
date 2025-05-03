package content.skill.magic

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.charge
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import WorldTest
import itemOnItem
import itemOption
import kotlin.test.assertEquals

abstract class DungeoneeringRewardStaffTest : WorldTest() {

    abstract val rune: String
    abstract val staff: String

    @Test
    fun `Add runes to staff`() {
        val player = createPlayer()
        player.inventory.add(rune, 10)
        player.inventory.add(staff)

        player.itemOnItem(0, 1)

        assertEquals(0, player.inventory.count(rune))
        assertEquals(10, player.inventory.charges(player, 1))
    }

    @Test
    fun `Add runes to mostly charged staff`() {
        val player = createPlayer()
        player.inventory.add(rune, 10)
        player.inventory.add(staff)
        player.inventory.charge(player, 1, 995)

        player.itemOnItem(0, 1)

        assertEquals(5, player.inventory.count(rune))
        assertEquals(1000, player.inventory.charges(player, 1))
    }

    @Test
    fun `Add runes to fully charged staff`() {
        val player = createPlayer()
        player.inventory.add(rune, 10)
        player.inventory.add(staff)
        player.inventory.charge(player, 1, 1000)

        player.itemOnItem(0, 1)

        assertEquals(10, player.inventory.count(rune))
        assertEquals(1000, player.inventory.charges(player, 1))
    }

    @Test
    fun `Empty staff with no charges`() {
        val player = createPlayer()
        player.inventory.add(rune, 10)
        player.inventory.add(staff)

        player.itemOption("Empty", staff)

        assertEquals(10, player.inventory.count(rune))
        assertEquals(0, player.inventory.charges(player, 1))
    }

    @Test
    fun `Empty staff with some charges`() {
        val player = createPlayer()
        player.inventory.add(rune, 10)
        player.inventory.add(staff)
        player.inventory.charge(player, 1, 123)

        player.itemOption("Empty", staff)

        assertEquals(133, player.inventory.count(rune))
        assertEquals(0, player.inventory.charges(player, 1))
    }

    @Test
    fun `Empty staff with full inventory`() {
        val player = createPlayer()
        player.inventory.add(staff)
        player.inventory.add("shark", 27)
        player.inventory.charge(player,0, 10)

        player.itemOption("Empty", staff)

        assertEquals(0, player.inventory.count(rune))
        assertEquals(10, player.inventory.charges(player, 0))
    }

    @Test
    fun `Empty staff with full inventory with runes`() {
        val player = createPlayer()
        player.inventory.add(staff)
        player.inventory.add(rune, 5)
        player.inventory.add("shark", 26)
        player.inventory.charge(player, 0, 10)

        player.itemOption("Empty", staff)

        assertEquals(15, player.inventory.count(rune))
        assertEquals(0, player.inventory.charges(player,  0))
    }

    @Test
    fun `Empty staff with almost full stack`() {
        val player = createPlayer()
        player.inventory.add(staff)
        player.inventory.add(rune, Int.MAX_VALUE - 5)
        player.inventory.add("shark", 26)
        player.inventory.charge(player, 0, 10)

        player.itemOption("Empty", staff)

        assertEquals(Int.MAX_VALUE, player.inventory.count(rune))
        assertEquals(5, player.inventory.charges(player, 0))
    }

    @Test
    fun `Empty staff with full stack`() {
        val player = createPlayer()
        player.inventory.add(staff)
        player.inventory.add(rune, Int.MAX_VALUE)
        player.inventory.add("shark", 26)
        player.inventory.charge(player, 0, 10)

        player.itemOption("Empty", staff)

        assertEquals(Int.MAX_VALUE, player.inventory.count(rune))
        assertEquals(10, player.inventory.charges(player, 0))
    }
}