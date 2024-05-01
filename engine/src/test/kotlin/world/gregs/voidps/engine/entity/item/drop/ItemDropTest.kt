package world.gregs.voidps.engine.entity.item.drop

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.NoRestrictions
import world.gregs.voidps.engine.inv.stack.ItemDependentStack

class ItemDropTest {

    @TestFactory
    fun `Item drop variable equals`() = listOf(true, false, "string", 1234, 1.23, 1234L).map { equals ->
        dynamicTest("Load item drop from map $equals") {
            val map = mapOf(
                "id" to "item",
                "amount" to 10..20,
                "variable" to "test",
                "equals" to equals
            )

            val drop = ItemDrop(map)

            assertEquals("item", drop.id)
            assertEquals(10..20, drop.amount)
            val variables = Player()
            assertFalse(drop.predicate!!.invoke(variables))
            variables["test"] = equals
            assertTrue(drop.predicate!!.invoke(variables))
        }
    }

    @TestFactory
    fun `Item drop variable equals default`() = listOf(true, false, "string", 1234, 1.23, 1234L).map { equals ->
        dynamicTest("Load item drop from map $equals") {
            val map = mapOf(
                "id" to "item",
                "amount" to 10..20,
                "variable" to "test",
                "equals" to equals,
                "default" to equals
            )

            val drop = ItemDrop(map)

            assertEquals("item", drop.id)
            assertEquals(10..20, drop.amount)
            val variables = Player()
            assertTrue(drop.predicate!!.invoke(variables))
        }
    }

    @Test
    fun `Item drop with owned item`() {
        val player = Player()
        val inventoryDefinitions = InventoryDefinitions(arrayOf(InventoryDefinition(length = 10)))
        inventoryDefinitions.ids = mapOf("inventory" to 0)
        player.inventories.definitions = inventoryDefinitions
        val itemDefinitions = ItemDefinitions(emptyArray()).apply { ids = mapOf("test" to 0) }
        player.inventories.normalStack = ItemDependentStack(itemDefinitions)
        player.inventories.validItemRule = NoRestrictions
        player.inventories.events = player
        val map = mapOf(
            "id" to "item",
            "amount" to 10,
            "owns" to "test"
        )

        val drop = ItemDrop(map)
        assertFalse(drop.predicate!!.invoke(player))
        assertTrue(player.inventory.add("test"))
        assertTrue(drop.predicate!!.invoke(player))
    }

    @Test
    fun `Item drop lacks item`() {
        val player = Player()
        val inventoryDefinitions = InventoryDefinitions(arrayOf(InventoryDefinition(length = 10)))
        inventoryDefinitions.ids = mapOf("inventory" to 0)
        player.inventories.definitions = inventoryDefinitions
        val itemDefinitions = ItemDefinitions(emptyArray()).apply { ids = mapOf("test" to 0) }
        player.inventories.normalStack = ItemDependentStack(itemDefinitions)
        player.inventories.validItemRule = NoRestrictions
        player.inventories.events = player
        val map = mapOf(
            "id" to "item",
            "amount" to 10,
            "lacks" to "test"
        )

        val drop = ItemDrop(map)
        assertTrue(drop.predicate!!.invoke(player))
        assertTrue(player.inventory.add("test"))
        assertFalse(drop.predicate!!.invoke(player))
    }

    @Test
    fun `Item drop owns and lacks items`() {
        val player = Player()
        val inventoryDefinitions = InventoryDefinitions(arrayOf(InventoryDefinition(length = 10)))
        inventoryDefinitions.ids = mapOf("inventory" to 0)
        player.inventories.definitions = inventoryDefinitions
        val itemDefinitions = ItemDefinitions(emptyArray()).apply { ids = mapOf("test" to 0, "unknown" to 1) }
        player.inventories.normalStack = ItemDependentStack(itemDefinitions)
        player.inventories.validItemRule = NoRestrictions
        player.inventories.events = player
        val map = mapOf(
            "id" to "item",
            "amount" to 10,
            "owns" to "test",
            "lacks" to "unknown"
        )
        val drop = ItemDrop(map)
        assertFalse(drop.predicate!!.invoke(player))
        assertTrue(player.inventory.add("test"))
        assertTrue(drop.predicate!!.invoke(player))
        assertTrue(player.inventory.add("unknown"))
        assertFalse(drop.predicate!!.invoke(player))
    }

    @Test
    fun `Item drop variable within range`() {
        val map = mapOf(
            "id" to "item",
            "amount" to 10..20,
            "variable" to "test",
            "within" to "1-10",
            "default" to 5
        )

        val drop = ItemDrop(map)
        val variables = Player()
        assertTrue(drop.predicate!!.invoke(variables))
        variables["test"] = 11
        assertFalse(drop.predicate!!.invoke(variables))
        variables["test"] = 10
        assertTrue(drop.predicate!!.invoke(variables))
    }

    @Test
    fun `Item drop from map`() {
        val map = mapOf(
            "id" to "item",
            "amount" to 1..5,
            "chance" to 5,
            "members" to true,
        )
        val drop = ItemDrop(map)
        assertEquals("item", drop.id)
        assertEquals(1..5, drop.amount)
        assertEquals(5, drop.chance)
        assertTrue(drop.members)
    }

    @Test
    fun `Item drop defaults`() {
        val map = mapOf(
            "id" to "item"
        )

        val drop = ItemDrop(map)
        assertEquals(1..1, drop.amount)
        assertEquals(1, drop.chance)
        assertFalse(drop.members)
        assertNull(drop.predicate)
    }

    @ParameterizedTest
    @ValueSource(strings = ["nothing", "", "  "])
    fun `Nothing drops are converted to empty items`(id: String) {
        val map = mapOf(
            "id" to id
        )

        val drop = ItemDrop(map)
        val item = drop.toItem()
        assertTrue(item.isEmpty())
    }

    @Test
    fun `Converted to item`() {
        val map = mapOf(
            "id" to "bones",
            "amount" to "1-5"
        )
        val drop = ItemDrop(map)
        val item = drop.toItem()
        assertFalse(item.isEmpty())
        assertEquals("bones", item.id)
        assertTrue(item.amount in 1..5)
    }

}