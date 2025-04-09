package world.gregs.voidps.engine.entity.item.drop

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.NoRestrictions
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DropTablesTest {

    private val tables = DropTables()

    @Test
    fun `Load from toml`() {
        val uri = DropTablesTest::class.java.getResource("drop-table.toml")!!.toURI()
        val decoder = DropTables().load(listOf(uri.path))
        val table = decoder.getValue("test_drop_table")
        assertNotNull(table)
        assertNull(decoder.get("invalid_drop_table"))

        assertEquals(TableType.All, table.type)
        assertEquals(1, table.chance)
        val drops = table.drops
        val bones = drops.first() as ItemDrop
        assertEquals("bones", bones.id)
        assertEquals(1..1, bones.amount)

        val subTable = drops.last() as DropTable
        assertEquals(TableType.First, subTable.type)
        assertEquals(1, subTable.chance)
        val subDrops = subTable.drops
        var drop = subDrops[0] as ItemDrop
        assertEquals("coins", drop.id)
        assertEquals(10..10, drop.amount)
        assertEquals(2, drop.chance)
        assertNull(drop.predicate)

        drop = subDrops[1] as ItemDrop
        assertEquals("coins", drop.id)
        assertEquals(1..1, drop.amount)
        assertEquals(1, drop.chance)
        assertNull(drop.predicate)

        drop = subDrops[2] as ItemDrop
        assertEquals("air_rune", drop.id)
        assertEquals(1..1, drop.amount)
        assertEquals(1, drop.chance)
        assertNotNull(drop.predicate)
        Settings.load(mapOf("world.members" to "true"))
        assertFalse(drop.predicate!!.invoke(Player()))
        Settings.load(mapOf("world.members" to "false"))
        assertTrue(drop.predicate!!.invoke(Player()))

        drop = subDrops[3] as ItemDrop
        assertEquals("dragon_dagger", drop.id)
        assertEquals(1..1, drop.amount)
        assertEquals(1, drop.chance)
        assertNotNull(drop.predicate)
        assertFalse(drop.predicate!!.invoke(Player()))
        Settings.load(mapOf("world.members" to "true"))
        assertTrue(drop.predicate!!.invoke(Player()))

        drop = subDrops[4] as ItemDrop
        assertEquals("coins", drop.id)
        assertEquals(1..5, drop.amount)
        assertEquals(1, drop.chance)
        assertNull(drop.predicate)
    }

    @TestFactory
    fun `Item drop variable equals`() = listOf(true, false, "string", 1234, 1.23, 1234L).map { equals ->
        dynamicTest("Load item drop from map $equals") {
            val drop = ItemDrop(
                id = "item",
                amount = 10..20,
                predicate = tables.dropPredicate(
                    variable = "test",
                    eq = equals
                )
            )

            Assertions.assertEquals("item", drop.id)
            Assertions.assertEquals(10..20, drop.amount)
            val variables = Player()
            Assertions.assertFalse(drop.predicate!!.invoke(variables))
            variables["test"] = equals
            Assertions.assertTrue(drop.predicate!!.invoke(variables))
        }
    }

    @TestFactory
    fun `Item drop variable equals default`() = listOf(true, false, "string", 1234, 1.23, 1234L).map { equals ->
        dynamicTest("Load item drop from map $equals") {
            val drop = ItemDrop(
                id = "item",
                amount = 10..20,
                predicate = tables.dropPredicate(
                    variable = "test",
                    eq = equals,
                    default = equals
                )
            )

            Assertions.assertEquals("item", drop.id)
            Assertions.assertEquals(10..20, drop.amount)
            val variables = Player()
            Assertions.assertTrue(drop.predicate!!.invoke(variables))
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
        val drop = ItemDrop(
            id = "item",
            amount = 10..10,

            predicate = tables.dropPredicate(
                owns = "test"
            )
        )
        Assertions.assertFalse(drop.predicate!!.invoke(player))
        Assertions.assertTrue(player.inventory.add("test"))
        Assertions.assertTrue(drop.predicate!!.invoke(player))
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
        val drop = ItemDrop(
            id = "item",
            amount = 10..10,
            predicate = tables.dropPredicate(
                lacks = "test"
            )
        )
        Assertions.assertTrue(drop.predicate!!.invoke(player))
        Assertions.assertTrue(player.inventory.add("test"))
        Assertions.assertFalse(drop.predicate!!.invoke(player))
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
        val drop = ItemDrop(
            id = "item",
            amount = 10..10,
            predicate = tables.dropPredicate(
                owns = "test",
                lacks = "unknown"
            )
        )
        Assertions.assertFalse(drop.predicate!!.invoke(player))
        Assertions.assertTrue(player.inventory.add("test"))
        Assertions.assertTrue(drop.predicate!!.invoke(player))
        Assertions.assertTrue(player.inventory.add("unknown"))
        Assertions.assertFalse(drop.predicate!!.invoke(player))
    }

    @Test
    fun `Item drop variable within range`() {
        val drop = ItemDrop(
            id = "item",
            amount = 10..20,
            predicate = tables.dropPredicate(
                variable = "test",
                withinMin = 1,
                withinMax = 10,
                default = 5,
            )
        )
        val variables = Player()
        Assertions.assertTrue(drop.predicate!!.invoke(variables))
        variables["test"] = 11
        Assertions.assertFalse(drop.predicate!!.invoke(variables))
        variables["test"] = 10
        Assertions.assertTrue(drop.predicate!!.invoke(variables))
    }

    @Test
    fun `Item drop from map`() {
        val drop = ItemDrop(
            id = "item",
            amount = 1..5,
            chance = 5,
            predicate = tables.dropPredicate(
                members = true
            )
        )
        Assertions.assertEquals("item", drop.id)
        Assertions.assertEquals(1..5, drop.amount)
        Assertions.assertEquals(5, drop.chance)
//        assertTrue(drop.members)
    }
}