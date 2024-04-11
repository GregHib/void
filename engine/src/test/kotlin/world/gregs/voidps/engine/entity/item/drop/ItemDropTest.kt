package world.gregs.voidps.engine.entity.item.drop

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player

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
            val variables = Variables(Player())
            assertFalse(drop.predicate!!.invoke(variables))
            variables.set("test", equals)
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
            val variables = Variables(Player())
            assertTrue(drop.predicate!!.invoke(variables))
        }
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
        val variables = Variables(Player())
        assertTrue(drop.predicate!!.invoke(variables))
        variables.set("test", 11)
        assertFalse(drop.predicate!!.invoke(variables))
        variables.set("test", 10)
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