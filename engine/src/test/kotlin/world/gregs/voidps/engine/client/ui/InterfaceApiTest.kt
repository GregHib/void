package world.gregs.voidps.engine.client.ui

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import kotlin.test.assertEquals

class InterfaceApiTest {

    @Nested
    inner class OnItemTest : ScriptTest {
        override val checks = listOf(
            listOf("id", "item"),
            listOf("id", "*"),
            listOf("*", "item"),
            listOf("*", "*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            onItem(args[0], args[1]) { item, id ->
                caller.call()
                assertEquals(Item("item"), item)
                assertEquals("id", id)
            }
        }

        override fun invoke(args: List<String>) {
            InterfaceApi.onItem(Player(), "id", Item("item"))
        }

        override val apis = listOf(InterfaceApi)

    }

    @Nested
    inner class ItemOnItemTest : ScriptTest {
        override val checks = listOf(
            listOf("from", "to"),
            listOf("from", "*"),
            listOf("*", "to"),
            listOf("*", "*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            itemOnItem(args[0], args[1], bidirectional = false) { fromItem, toItem, fromSlot, toSlot ->
                caller.call()
                assertEquals(Item("from"), fromItem)
                assertEquals(Item("to"), toItem)
                assertEquals(1, fromSlot)
                assertEquals(2, toSlot)
            }
        }

        override fun invoke(args: List<String>) {
            InterfaceApi.itemOnItem(Player(), Item("from"), Item("to"), 1, 2)
        }

        override val apis = listOf(InterfaceApi)

    }

    @Nested
    inner class InterfaceOpenedTest : ScriptTest {
        override val checks = listOf(
            listOf("id"),
        )
        override val failedChecks = listOf(
            listOf("*"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            interfaceOpened(args[0]) { id ->
                caller.call()
                assertEquals("id", id)
            }
        }

        override fun invoke(args: List<String>) {
            InterfaceApi.open(Player(), "id")
        }

        override val apis = listOf(InterfaceApi)

    }

    @Nested
    inner class InterfaceClosedTest : ScriptTest {
        override val checks = listOf(
            listOf("id"),
        )
        override val failedChecks = listOf(
            listOf("*"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            interfaceClosed(args[0]) { id ->
                caller.call()
                assertEquals("id", id)
            }
        }

        override fun invoke(args: List<String>) {
            InterfaceApi.close(Player(), "id")
        }

        override val apis = listOf(InterfaceApi)

    }

    @Nested
    inner class InterfaceRefreshTest : ScriptTest {
        override val checks = listOf(
            listOf("id"),
        )
        override val failedChecks = listOf(
            listOf("*"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            interfaceRefresh(args[0]) { id ->
                caller.call()
                assertEquals("id", id)
            }
        }

        override fun invoke(args: List<String>) {
            InterfaceApi.refresh(Player(), "id")
        }

        override val apis = listOf(InterfaceApi)

    }

    @Nested
    inner class InterfaceSwapTest : ScriptTest {
        override val checks = listOf(
            listOf("from", "to"),
            listOf("from", "*"),
            listOf("*", "to"),
        )
        override val failedChecks = listOf(
            listOf("*", "*"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            interfaceSwap(args[0], args[1]) { fromId, toId, fromSlot, toSlot ->
                caller.call()
                assertEquals("from", fromId)
                assertEquals("to", toId)
                assertEquals(1, fromSlot)
                assertEquals(2, toSlot)
            }
        }

        override fun invoke(args: List<String>) {
            InterfaceApi.swap(Player(), "from", "to", 1, 2)
        }

        override val apis = listOf(InterfaceApi)

    }

    @Nested
    inner class InterfaceOptionTest : ScriptTest {
        override val checks = listOf(
            listOf("option", "id:comp"),
            listOf("option", "*"),
            listOf("*", "id:comp"),
        )
        override val failedChecks = listOf(
            listOf("*", "*"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            interfaceOption(args[0], args[1]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            runTest {
                InterfaceApi.option(Player(), InterfaceOption(Item.EMPTY, 0, "option", 1, "id:comp"))
            }
        }

        override val apis = listOf(InterfaceApi)

    }

    @Nested
    inner class ItemOptionTest : ScriptTest {
        override val checks = listOf(
            listOf("option", "item", "inv"),
            listOf("option", "*", "inv"),
            listOf("option", "item", "*"),
            listOf("option", "*", "*"),
        )
        override val failedChecks = listOf(
            listOf("*", "item", "inv"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            itemOption(args[0], args[1], args[2]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            InterfaceApi.itemOption(Player(), "option", Item("item"), 0, "inv")
        }

        override val apis = listOf(InterfaceApi)

    }

    @Nested
    inner class QuestJournalOpenTest : ScriptTest {
        override val checks = listOf(
            listOf("quest"),
        )
        override val failedChecks = listOf(
            listOf("*"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            questJournalOpen(args[0]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            InterfaceApi.openQuestJournal(Player(), "quest")
        }

        override val apis = listOf(InterfaceApi)

    }

    @Nested
    inner class OpenShopTest : ScriptTest {
        override val checks = listOf(
            listOf("shop"),
            listOf("*"),
        )
        override val failedChecks =  emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            shopOpen(args[0]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            InterfaceApi.openShop(Player(), "shop")
        }

        override val apis = listOf(InterfaceApi)

    }


}