package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
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

    companion object {
        val onItem = mutableMapOf<String, MutableList<Player.(Item, String) -> Unit>>()
        val itemOnItem = mutableMapOf<String, MutableList<Player.(Item, Item, Int, Int) -> Unit>>()

        fun onItem(player: Player, id: String, item: Item) {
            for (block in onItem["$id:${item.id}"] ?: onItem["*:${item.id}"] ?: onItem["$id:*"] ?: onItem["*:*"] ?: return) {
                block(player, item, id)
            }
        }

        fun itemOnItem(player: Player, from: Item, to: Item, fromSlot: Int, toSlot: Int) {
            for (block in itemOnItem["${from.id}:${to.id}"] ?: itemOnItem["*:${to.id}"] ?:  itemOnItem["${from.id}:*"] ?:  itemOnItem["*:*"] ?: return) {
                block(player, from, to, fromSlot, toSlot)
            }
        }

        fun clear() {
            onItem.clear()
            itemOnItem.clear()
        }
    }
}