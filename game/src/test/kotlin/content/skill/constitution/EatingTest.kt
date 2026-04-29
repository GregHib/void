package content.skill.constitution

import WorldTest
import itemOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

internal class EatingTest : WorldTest() {

    @Test
    fun `Eating shark mid-attack extends action_delay by 3 ticks`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("shark")
        player.start("action_delay", 4)
        tick(2)

        player.itemOption("Eat", "shark")

        assertEquals(5, player.remaining("action_delay"))
        assertEquals(3, player.remaining("food_delay"))
    }

    @Test
    fun `Shark then karambwan combo stacks attack delay`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("shark")
        player.inventory.add("cooked_karambwan")
        player.start("action_delay", 1)

        player.itemOption("Eat", "shark")

        assertEquals(4, player.remaining("action_delay"))
        assertEquals(3, player.remaining("food_delay"))

        player.itemOption("Eat", "cooked_karambwan")

        assertEquals(6, player.remaining("action_delay"))
        assertEquals(3, player.remaining("combo_delay"))
    }

    @Test
    fun `Pizza out of combat does not extend action_delay`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("anchovy_pizza")
        assertFalse(player.hasClock("action_delay"))

        player.itemOption("Eat", "anchovy_pizza")

        assertFalse(player.hasClock("action_delay"))
        assertEquals(1, player.remaining("food_delay"))
        assertTrue(player.inventory.contains("anchovy_pizza_half"))
    }

    @Test
    fun `Anchovy pizza half eat_delay is 2 ticks`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("anchovy_pizza_half")

        player.itemOption("Eat", "anchovy_pizza_half")

        assertEquals(2, player.remaining("food_delay"))
    }

    @Test
    fun `Drinking brew mid-attack does not extend action_delay`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("saradomin_brew_4")
        player.start("action_delay", 4)

        player.itemOption("Drink", "saradomin_brew_4")

        assertEquals(2, player.remaining("drink_delay"))
        assertEquals(4, player.remaining("action_delay"))
    }

    @Test
    fun `Combo food bypasses active food_delay block`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("shark")
        player.inventory.add("cooked_karambwan")

        player.itemOption("Eat", "shark")
        assertEquals(3, player.remaining("food_delay"))

        player.itemOption("Eat", "cooked_karambwan")

        assertEquals(3, player.remaining("combo_delay"))
        assertFalse(player.inventory.contains("cooked_karambwan"))
    }
}
