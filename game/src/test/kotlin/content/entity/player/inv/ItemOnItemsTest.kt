package content.entity.player.inv

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.event.IntEntered
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import WorldTest
import itemOnItem
import kotlin.test.assertEquals

class ItemOnItemsTest : WorldTest() {

    @Test
    fun `Combine full inventory of items`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Cooking, 50)
        player.inventory.add("cake", 14)
        player.inventory.add("chocolate_dust", 14)

        player.itemOnItem(12, 14)
        val amount = 14
        tick(1)
        player["skill_creation_amount"] = amount
        player.emit(IntEntered(0))
        tick(amount * 2)

        assertEquals(0, player.inventory.count("cake"))
        assertEquals(0, player.inventory.count("chocolate_dust"))
        assertEquals(14, player.inventory.count("chocolate_cake"))
    }

    @Test
    fun `Combine part of a full inventory of items`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Cooking, 50)
        player.inventory.add("cake", 14)
        player.inventory.add("chocolate_dust", 14)

        player.itemOnItem(12, 14)

        val amount = 5
        tick(1)
        player["skill_creation_amount"] = amount
        player.emit(IntEntered(0))
        tick(amount * 2)

        assertEquals(9, player.inventory.count("cake"))
        assertEquals(9, player.inventory.count("chocolate_dust"))
        assertEquals(amount, player.inventory.count("chocolate_cake"))
    }

    @Test
    fun `Item on item with not enough items`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Cooking, 50)
        player.inventory.add("cake", 1)
        player.inventory.add("chocolate_dust", 2)

        player.itemOnItem(0, 1)
        val amount = 2
        tick(1)
        player["skill_creation_amount"] = amount
        player.emit(IntEntered(0))
        tick(amount * 2)

        assertEquals(0, player.inventory.count("cake"))
        assertEquals(1, player.inventory.count("chocolate_dust"))
        assertEquals(1, player.inventory.count("chocolate_cake"))
    }

    @Test
    fun `Stackable item on item creation with full inventory of items`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Fletching, 50)
        player.inventory.add("opal_bolt_tips", 10)
        player.inventory.add("bronze_bolts", 10)
        player.inventory.add("opal_bolts", 10)
        player.inventory.add("shark", 25)

        player.itemOnItem(0, 1)
        tick(2)

        assertEquals(0, player.inventory.count("opal_bolt_tips"))
        assertEquals(0, player.inventory.count("bronze_bolts"))
        assertEquals(20, player.inventory.count("opal_bolts"))
    }

}