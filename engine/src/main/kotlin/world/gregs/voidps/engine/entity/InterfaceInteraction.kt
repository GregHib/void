package world.gregs.voidps.engine.entity

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

interface InterfaceInteraction {

    fun onItem(id: String, item: String = "*", block: Player.(Item, String) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(item, Wildcard.Item) { itm ->
                onItem.getOrPut("$i:$itm") { mutableListOf() }.add(block)
            }
        }
    }

    fun itemOnItem(fromItem: String = "*", toItem: String = "*", bidirectional: Boolean = true, block: Player.(fromItem: Item, toItem: Item, fromSlot: Int, toSlot: Int) -> Unit) {
        val biHandler: Player.(Item, Item, Int, Int) -> Unit = { from, to, fromSlot, toSlot ->
            block(this, to, from, toSlot, fromSlot)
        }
        append(fromItem, toItem, bidirectional, biHandler, block)
    }

    fun itemOnItem(fromItem: String = "*", toItem: String = "*", bidirectional: Boolean = true, block: Player.(fromItem: Item, toItem: Item) -> Unit) {
        val handler: Player.(Item, Item, Int, Int) -> Unit = { from, to, _, _ ->
            block(this, from, to)
        }
        val biHandler: Player.(Item, Item, Int, Int) -> Unit = { from, to, _, _ ->
            block(this, to, from)
        }
        append(fromItem, toItem, bidirectional, biHandler, handler)
    }

    /**
     * Notification that an interface was opened.
     * @see [interfaceRefresh] for re-opened interfaces
     */
    fun interfaceOpen(id: String, block: Player.(id: String) -> Unit) {
        Wildcards.find(id, Wildcard.Interface) { i ->
            opened.getOrPut(i) { mutableListOf() }.add(block)
        }
    }

    /**
     * An interface was open and has now been closed
     */
    fun interfaceClose(id: String, block: Player.(id: String) -> Unit) {
        Wildcards.find(id, Wildcard.Interface) { i ->
            closed.getOrPut(i) { mutableListOf() }.add(block)
        }
    }

    /**
     * When an interface is initially opened or opened again
     * Primarily for interface changes like unlocking.
     */
    fun interfaceRefresh(id: String, block: Player.(id: String) -> Unit) {
        Wildcards.find(id, Wildcard.Interface) { i ->
            refreshed.getOrPut(i) { mutableListOf() }.add(block)
        }
    }

    fun interfaceSwap(fromId: String = "*", toId: String = "*", block: Player.(fromId: String, toId: String, fromSlot: Int, toSlot: Int) -> Unit) {
        Wildcards.find(fromId, Wildcard.Component) { from ->
            Wildcards.find(toId, Wildcard.Component) { to ->
                swapped.getOrPut("$from:$to") { mutableListOf() }.add(block)
            }
        }
    }

    fun interfaceOption(option: String = "*", id: String = "*", block: suspend Player.(InterfaceOption) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            options.getOrPut("$option:$i") { mutableListOf() }.add(block)
        }
    }

    fun itemOption(option: String, item: String = "*", inventory: String = "inventory", block: suspend Player.(ItemOption) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { i ->
            itemOption.getOrPut("$option:$i:$inventory") { mutableListOf() }.add(block)
        }
    }

    private fun append(
        fromItem: String,
        toItem: String,
        bidirectional: Boolean,
        biHandler: Player.(Item, Item, Int, Int) -> Unit,
        handler: Player.(from: Item, to: Item, fromSlot: Int, toSlot: Int) -> Unit,
    ) {
        Wildcards.find(fromItem, Wildcard.Item) { from ->
            Wildcards.find(toItem, Wildcard.Item) { to ->
                if (bidirectional) {
                    itemOnItem.getOrPut("$to:$from") { mutableListOf() }.add(biHandler)
                }
                itemOnItem.getOrPut("$from:$to") { mutableListOf() }.add(handler)
            }
        }
    }

    fun questJournalOpen(quest: String, block: Player.() -> Unit) {
        quests[quest] = block
    }

    companion object : AutoCloseable {
        private val quests = Object2ObjectOpenHashMap<String, Player.() -> Unit>(20)
        private val opened = Object2ObjectOpenHashMap<String, MutableList<Player.(String) -> Unit>>(150)
        private val closed = Object2ObjectOpenHashMap<String, MutableList<Player.(String) -> Unit>>(75)
        private val refreshed = Object2ObjectOpenHashMap<String, MutableList<Player.(String) -> Unit>>(25)
        private val swapped = Object2ObjectOpenHashMap<String, MutableList<Player.(String, String, Int, Int) -> Unit>>(10)
        private val onItem = Object2ObjectOpenHashMap<String, MutableList<Player.(Item, String) -> Unit>>(2)
        private val itemOnItem = Object2ObjectOpenHashMap<String, MutableList<Player.(Item, Item, Int, Int) -> Unit>>(800)
        private val options = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceOption) -> Unit>>(50)
        private val itemOption = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemOption) -> Unit>>(600)

        suspend fun option(player: Player, click: InterfaceOption) {
            for (block in options["${click.option}:${click.interfaceComponent}"] ?: options["*:${click.interfaceComponent}"] ?: options["${click.option}:*"] ?: return) {
                block(player, click)
            }
        }

        fun itemOption(player: Player, option: String, item: Item = Item.EMPTY, slot: Int = 0, inventory: String = "inventory") {
            Events.events.launch {
                itemOption(player, ItemOption(item, slot, inventory, option))
            }
        }

        suspend fun itemOption(player: Player, click: ItemOption) {
            for (block in itemOption["${click.option}:${click.item.id}:${click.inventory}"] ?: itemOption["${click.option}:*:${click.inventory}"] ?: itemOption["${click.option}:${click.item.id}:*"] ?: itemOption["${click.option}:*:*"] ?: return) {
                block(player, click)
            }
        }

        fun openQuestJournal(player: Player, quest: String) {
            quests[quest]?.invoke(player)
        }

        fun open(player: Player, id: String) {
            for (block in opened[id] ?: return) {
                block(player, id)
            }
        }

        fun close(player: Player, id: String) {
            for (block in closed[id] ?: return) {
                block(player, id)
            }
        }

        fun refresh(player: Player, id: String) {
            for (block in refreshed[id] ?: return) {
                block(player, id)
            }
        }

        fun swap(player: Player, fromId: String, toId: String, fromSlot: Int, toSlot: Int) {
            for (block in swapped["$fromId:$toId"] ?: swapped["$fromId:*"] ?: swapped["*:$toId"] ?: return) {
                block(player, fromId, toId, fromSlot, toSlot)
            }
        }

        fun onItem(player: Player, id: String, item: Item) {
            for (block in onItem["$id:${item.id}"] ?: onItem["*:${item.id}"] ?: onItem["$id:*"] ?: onItem["*:*"] ?: return) {
                block(player, item, id)
            }
        }

        fun itemOnItem(player: Player, from: Item, to: Item, fromSlot: Int, toSlot: Int) {
            for (block in itemOnItem["${from.id}:${to.id}"] ?: itemOnItem["*:${to.id}"] ?: itemOnItem["${from.id}:*"] ?: itemOnItem["*:*"] ?: return) {
                block(player, from, to, fromSlot, toSlot)
            }
        }

        override fun close() {
            opened.clear()
            closed.clear()
            refreshed.clear()
            swapped.clear()
            onItem.clear()
            itemOnItem.clear()
            options.clear()
            itemOption.clear()
        }
    }
}