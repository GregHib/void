package world.gregs.voidps.engine.entity

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

interface InterfaceInteraction {

    fun onItem(id: String, item: String = "*", handler: Player.(Item, String) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(item, Wildcard.Item) { itm ->
                onItem.getOrPut("$i:$itm") { mutableListOf() }.add(handler)
            }
        }
    }

    fun itemOnItem(fromItem: String = "*", toItem: String = "*", bidirectional: Boolean = true, handler: Player.(fromItem: Item, toItem: Item, fromSlot: Int, toSlot: Int) -> Unit) {
        val biHandler: Player.(Item, Item, Int, Int) -> Unit = { from, to, fromSlot, toSlot ->
            handler(this, to, from, toSlot, fromSlot)
        }
        append(fromItem, toItem, bidirectional, biHandler, handler)
    }

    fun itemOnItem(fromItem: String = "*", toItem: String = "*", bidirectional: Boolean = true, handler: Player.(fromItem: Item, toItem: Item) -> Unit) {
        val single: Player.(Item, Item, Int, Int) -> Unit = { from, to, _, _ ->
            handler(this, from, to)
        }
        val biHandler: Player.(Item, Item, Int, Int) -> Unit = { from, to, _, _ ->
            handler(this, to, from)
        }
        append(fromItem, toItem, bidirectional, biHandler, single)
    }

    /**
     * Notification that an interface was opened.
     * @see [interfaceRefresh] for re-opened interfaces
     */
    fun interfaceOpen(id: String, handler: Player.(id: String) -> Unit) {
        Wildcards.find(id, Wildcard.Interface) { i ->
            opened.getOrPut(i) { mutableListOf() }.add(handler)
        }
    }

    /**
     * An interface was open and has now been closed
     */
    fun interfaceClose(id: String, handler: Player.(id: String) -> Unit) {
        Wildcards.find(id, Wildcard.Interface) { i ->
            closed.getOrPut(i) { mutableListOf() }.add(handler)
        }
    }

    /**
     * When an interface is initially opened or opened again
     * Primarily for interface changes like unlocking.
     */
    fun interfaceRefresh(id: String, handler: Player.(id: String) -> Unit) {
        Wildcards.find(id, Wildcard.Interface) { i ->
            refreshed.getOrPut(i) { mutableListOf() }.add(handler)
        }
    }

    fun interfaceSwap(fromId: String = "*", toId: String = "*", handler: Player.(fromId: String, toId: String, fromSlot: Int, toSlot: Int) -> Unit) {
        Wildcards.find(fromId, Wildcard.Component) { from ->
            Wildcards.find(toId, Wildcard.Component) { to ->
                swapped.getOrPut("$from:$to") { mutableListOf() }.add(handler)
            }
        }
    }

    fun interfaceOption(option: String = "*", id: String = "*", handler: suspend Player.(InterfaceOption) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            options.getOrPut("$option:$i") { mutableListOf() }.add(handler)
        }
    }

    fun itemOption(option: String, item: String = "*", inventory: String = "inventory", handler: suspend Player.(ItemOption) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { i ->
            itemOption.getOrPut("$option:$i:$inventory") { mutableListOf() }.add(handler)
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

    fun questJournalOpen(quest: String, handler: Player.() -> Unit) {
        quests[quest] = handler
    }

    fun shopOpen(shop: String = "*", handler: Player.(String) -> Unit) {
        shops[shop] = handler
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
        private val shops = Object2ObjectOpenHashMap<String, (Player, String) -> Unit>(5)

        suspend fun option(player: Player, click: InterfaceOption) {
            for (block in options["${click.option}:${click.interfaceComponent}"] ?: options["*:${click.interfaceComponent}"] ?: options["${click.option}:*"] ?: return) {
                block(player, click)
            }
        }

        fun itemOption(player: Player, option: String, item: Item = Item.EMPTY, slot: Int = 0, inventory: String = "inventory") {
            Script.launch {
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

        fun openShop(player: Player, id: String) {
            shops[id]?.invoke(player, id)
            shops["*"]?.invoke(player, id)
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