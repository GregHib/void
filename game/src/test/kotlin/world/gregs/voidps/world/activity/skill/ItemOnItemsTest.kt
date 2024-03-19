package world.gregs.voidps.world.activity.skill

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.event.IntEntered
import world.gregs.voidps.engine.client.ui.interact.ItemOnItem
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.script.WorldTest
import kotlin.test.assertEquals

class ItemOnItemsTest : WorldTest() {

    @Test
    fun `Combine full inventory of items`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Cooking, 50)
        player.inventory.add("cake", 14)
        player.inventory.add("chocolate_dust", 14)

        player.emit(ItemOnItem(
            fromItem = Item("cake"),
            toItem = Item("chocolate_dust"),
            fromSlot = 12,
            toSlot = 14,
            fromInterface = "inventory",
            fromComponent = "inventory",
            toInterface = "inventory",
            toComponent = "inventory",
            fromInventory = "inventory",
            toInventory = "inventory",
        ))
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

        player.emit(ItemOnItem(
            fromItem = Item("cake"),
            toItem = Item("chocolate_dust"),
            fromSlot = 12,
            toSlot = 14,
            fromInterface = "inventory",
            fromComponent = "inventory",
            toInterface = "inventory",
            toComponent = "inventory",
            fromInventory = "inventory",
            toInventory = "inventory",
        ))
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

        player.emit(ItemOnItem(
            fromItem = Item("cake"),
            toItem = Item("chocolate_dust"),
            fromSlot = 0,
            toSlot = 1,
            fromInterface = "inventory",
            fromComponent = "inventory",
            toInterface = "inventory",
            toComponent = "inventory",
            fromInventory = "inventory",
            toInventory = "inventory",
        ))
        val amount = 2
        tick(1)
        player["skill_creation_amount"] = amount
        player.emit(IntEntered(0))
        tick(amount * 2)

        assertEquals(0, player.inventory.count("cake"))
        assertEquals(1, player.inventory.count("chocolate_dust"))
        assertEquals(1, player.inventory.count("chocolate_cake"))
    }
}