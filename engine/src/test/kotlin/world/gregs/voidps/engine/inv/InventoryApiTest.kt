package world.gregs.voidps.engine.inv

import org.junit.jupiter.api.Nested
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

class InventoryApiTest {

    @Nested
    inner class SlotChangedTest : ScriptTest {
        override val checks = listOf(
            listOf("inv", "1"),
            listOf("*", "1"),
            listOf("inv", "*"),
            listOf("*", "*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            slotChanged(args[0], args[1].toIntOrNull()) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            InventoryApi.changed(Player(), InventorySlotChanged(
                "inv",
                1,
                Item("item"),
                "from",
                2,
                Item("from"),
            ))
        }

        override val apis = listOf(InventoryApi)

    }

    @Nested
    inner class ItemAddedTest : ScriptTest {
        override val checks = listOf(
            listOf("item", "inv", "1"),
            listOf("*", "inv", "1"),
            listOf("item", "inv", "*"),
            listOf("*", "inv", "*"),
        )
        override val failedChecks = listOf(
            listOf("item", "*", "1"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            itemAdded(args[0], args[1], args[2].toIntOrNull()) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            InventoryApi.add(Player(), ItemAdded(
                Item("item"),
                "inv",
                1,
            ))
        }

        override val apis = listOf(InventoryApi)

    }

    @Nested
    inner class ItemRemovedTest : ScriptTest {
        override val checks = listOf(
            listOf("item", "inv", "1"),
            listOf("*", "inv", "1"),
            listOf("item", "inv", "*"),
            listOf("*", "inv", "*"),
        )
        override val failedChecks = listOf(
            listOf("item", "*", "1"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            itemRemoved(args[0], args[1], args[2].toIntOrNull()) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            InventoryApi.remove(Player(), ItemRemoved(
                "inv",
                1,
                Item("item"),
            ))
        }

        override val apis = listOf(InventoryApi)

    }

}