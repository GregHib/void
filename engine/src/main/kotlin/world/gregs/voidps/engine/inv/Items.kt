package world.gregs.voidps.engine.inv

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

interface Items {

    /**
     * Do something after an item is bought from a shop
     */
    fun bought(item: String = "*", handler: Player.(Item) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { i ->
            bought[i] = handler
        }
    }

    /**
     * Do something after an item is sold to a shop
     */
    fun sold(item: String = "*", handler: Player.(Item) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { i ->
            sold[i] = handler
        }
    }

    /**
     * Check if an item can be dropped
     */
    fun droppable(handler: Player.(Item) -> Boolean) {
        droppable.add(handler)
    }

    /**
     * Do something after an item is dropped
     */
    fun dropped(item: String, handler: Player.(Item) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { i ->
            dropped[i] = handler
        }
    }

    /**
     * Check if an item can be destroyed
     */
    fun destructible(item: String, handler: Player.(Item) -> Boolean) {
        Wildcards.find(item, Wildcard.Item) { i ->
            destroyable[i] = handler
        }
    }

    /**
     * Do something after an item is destroyed
     */
    fun destroyed(item: String, handler: Player.(Item) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { i ->
            destroyed[i] = handler
        }
    }

    /**
     * Check if an item can be eaten or drunk
     */
    fun consumable(item: String, handler: Player.(Item) -> Boolean) {
        Wildcards.find(item, Wildcard.Item) { i ->
            consumable[i] = handler
        }
    }

    /**
     * Apply effects after an item has been eaten or drank
     */
    fun consumed(item: String = "*", handler: Player.(Item, Int) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { i ->
            consumed[i] = handler
        }
    }

    /**
     * When an item was taken off of the floor
     */
    fun taken(item: String, handler: Player.(FloorItem) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { i ->
            taken[i] = handler
        }
    }

    /**
     * Returns id of an item to take off of the floor or null to cancel.
     */
    fun takeable(item: String, handler: Player.(item: String) -> String?) {
        Wildcards.find(item, Wildcard.Item) { i ->
            takeable[i] = handler
        }
    }

    /**
     * Do something after items are crafted together
     */
    fun crafted(skill: Skill? = null, handler: Player.(ItemOnItemDefinition) -> Unit) {
        crafted.getOrPut(skill) { mutableListOf() }.add(handler)
    }

    companion object : AutoCloseable {
        private val dropped = Object2ObjectOpenHashMap<String, (Player, Item) -> Unit>(2)
        private val droppable = ObjectArrayList<(Player, Item) -> Boolean>(2)
        private val bought = Object2ObjectOpenHashMap<String, (Player, Item) -> Unit>(5)
        private val sold = Object2ObjectOpenHashMap<String, (Player, Item) -> Unit>(5)
        private val taken = Object2ObjectOpenHashMap<String, (Player, FloorItem) -> Unit>(5)
        private val takeable = Object2ObjectOpenHashMap<String, (Player, String) -> String?>(2)
        private val destroyed = Object2ObjectOpenHashMap<String, (Player, Item) -> Unit>(5)
        private val destroyable = Object2ObjectOpenHashMap<String, (Player, Item) -> Boolean>(2)
        private val consumed = Object2ObjectOpenHashMap<String, (Player, Item, Int) -> Unit>(5)
        private val consumable = Object2ObjectOpenHashMap<String, (Player, Item) -> Boolean>(125)
        private val crafted = Object2ObjectOpenHashMap<Skill?, MutableList<(Player, ItemOnItemDefinition) -> Unit>>(5)

        fun takeable(player: Player, item: String): String? {
            val handler = takeable[item] ?: return item
            return handler.invoke(player, item)
        }

        fun take(player: Player, floorItem: FloorItem) {
            taken[floorItem.id]?.invoke(player, floorItem)
        }

        fun craft(player: Player, def: ItemOnItemDefinition) {
            for (handler in crafted[def.skill] ?: return) {
                handler(player, def)
            }
        }

        fun droppable(player: Player, item: Item): Boolean {
            var canDrop = true
            for (handler in droppable) {
                if (!handler(player, item)) {
                    canDrop = false
                }
            }
            return canDrop
        }

        fun drop(player: Player, item: Item) {
            dropped[item.id]?.invoke(player, item)
        }

        fun destructible(player: Player, item: Item): Boolean {
            return destroyable[item.id]?.invoke(player, item) ?: false
        }

        fun destroyed(player: Player, item: Item) {
            destroyed[item.id]?.invoke(player, item)
        }

        fun consumable(player: Player, item: Item): Boolean {
            return consumable[item.id]?.invoke(player, item) ?: false
        }

        fun consume(player: Player, item: Item, slot: Int) {
            consumed[item.id]?.invoke(player, item, slot)
            consumed["*"]?.invoke(player, item, slot)
        }

        fun bought(player: Player, item: Item) {
            bought[item.id]?.invoke(player, item)
            bought["*"]?.invoke(player, item)
        }

        fun sold(player: Player, item: Item) {
            sold[item.id]?.invoke(player, item)
            sold["*"]?.invoke(player, item)
        }

        override fun close() {
            dropped.clear()
            droppable.clear()
            bought.clear()
            sold.clear()
            taken.clear()
            takeable.clear()
            destroyed.clear()
            destroyable.clear()
            consumed.clear()
            consumable.clear()
            crafted.clear()
        }
    }
}