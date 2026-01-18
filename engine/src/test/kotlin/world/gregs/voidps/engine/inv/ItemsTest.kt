package world.gregs.voidps.engine.inv

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ItemsTest {

    @Nested
    inner class BoughtTest : ScriptTest {
        override val checks = listOf(
            listOf("item"),
            listOf("*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            bought(args[0]) { item ->
                caller.call()
                assertEquals(Item("item"), item)
            }
        }

        override fun invoke(args: List<String>) {
            Items.bought(Player(), Item("item"))
        }

        override val apis = listOf(Items)

    }

    @Nested
    inner class SoldTest : ScriptTest {
        override val checks = listOf(
            listOf("item"),
            listOf("*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            sold(args[0]) { item ->
                caller.call()
                assertEquals(Item("item"), item)
            }
        }

        override fun invoke(args: List<String>) {
            Items.sold(Player(), Item("item"))
        }

        override val apis = listOf(Items)

    }

    @Nested
    inner class DroppableTest : ScriptTest {
        override val checks = listOf(
            listOf<String>(),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            droppable { item ->
                caller.call()
                assertEquals(Item("item"), item)
                true
            }
        }

        override fun invoke(args: List<String>) {
            assertTrue(Items.droppable(Player(), Item("item")))
        }

        override val apis = listOf(Items)

    }

    @Nested
    inner class DroppedTest : ScriptTest {
        override val checks = listOf(
            listOf("item"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            dropped(args[0]) { item ->
                caller.call()
                assertEquals(Item("item"), item)
            }
        }

        override fun invoke(args: List<String>) {
            Items.drop(Player(), Item("item"))
        }

        override val apis = listOf(Items)

    }

    @Nested
    inner class DestructibleTest : ScriptTest {
        override val checks = listOf(
            listOf("item"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            destructible(args[0]) { item ->
                caller.call()
                assertEquals(Item("item"), item)
                true
            }
        }

        override fun invoke(args: List<String>) {
            assertTrue(Items.destructible(Player(), Item("item")))
        }

        override val apis = listOf(Items)

    }

    @Nested
    inner class DestroyedTest : ScriptTest {
        override val checks = listOf(
            listOf("item"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            destroyed(args[0]) { item ->
                caller.call()
                assertEquals(Item("item"), item)
            }
        }

        override fun invoke(args: List<String>) {
            Items.destroyed(Player(), Item("item"))
        }

        override val apis = listOf(Items)

    }

    @Nested
    inner class ConsumableTest : ScriptTest {
        override val checks = listOf(
            listOf("item"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            consumable(args[0]) { item ->
                caller.call()
                assertEquals(Item("item"), item)
                true
            }
        }

        override fun invoke(args: List<String>) {
            assertTrue(Items.consumable(Player(), Item("item")))
        }

        override val apis = listOf(Items)

    }

    @Nested
    inner class ConsumedTest : ScriptTest {
        override val checks = listOf(
            listOf("item"),
            listOf("*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            consumed(args[0]) { item, slot ->
                caller.call()
                assertEquals(Item("item"), item)
                assertEquals(2, slot)
            }
        }

        override fun invoke(args: List<String>) {
            Items.consume(Player(), Item("item"), 2)
        }

        override val apis = listOf(Items)

    }

    @Nested
    inner class TakeableTest : ScriptTest {
        override val checks = listOf(
            listOf("item"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            takeable(args[0]) { item ->
                caller.call()
                assertEquals("item", item)
                "string"
            }
        }

        override fun invoke(args: List<String>) {
            val actual = Items.takeable(Player(), "item")
            assertTrue(actual == "item" || actual == "string")
        }

        override val apis = listOf(Items)

    }

    @Nested
    inner class TakenTest : ScriptTest {
        override val checks = listOf(
            listOf("item"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            taken(args[0]) { item ->
                caller.call()
                assertEquals("item", item.id)
            }
        }

        override fun invoke(args: List<String>) {
            Items.take(Player(), FloorItem(Tile.EMPTY, "item"))
        }

        override val apis = listOf(Items)

    }

    @Nested
    inner class CraftedTest : ScriptTest {
        override val checks = listOf(
            listOf("Attack"),
        )
        override val failedChecks = listOf(
            listOf("Strength"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            crafted(args.getOrNull(0)?.let { Skill.of(it) }) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            runTest {
                Items.craft(Player(), ItemOnItemDefinition(Skill.Attack))
            }
        }

        override val apis = listOf(Items)

    }

}