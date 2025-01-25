package content.entity.player.inv

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class InventoryOption(
    override val character: Player,
    val inventory: String,
    val item: Item,
    val slot: Int,
    val option: String
) : Interaction<Player>() {

    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "inventory_option"
        1 -> option
        2 -> item.id
        3 -> inventory
        else -> null
    }
}

fun inventoryOptions(vararg options: String = arrayOf("*"), item: String = "*", inventory: String = "*", block: suspend InventoryOption.() -> Unit) {
    val handler: suspend InventoryOption.(EventDispatcher) -> Unit = {
        block.invoke(this)
    }
    for (option in options) {
        Events.handle("inventory_option", option, item, inventory, handler = handler)
    }
}

fun inventoryOption(option: String = "*", inventory: String = "*", handler: suspend InventoryOption.() -> Unit) {
    Events.handle<InventoryOption>("inventory_option", option, "*", inventory) {
        handler.invoke(this)
    }
}

fun inventoryItem(option: String = "*", item: String = "*", inventory: String = "*", handler: suspend InventoryOption.() -> Unit) {
    Events.handle<InventoryOption>("inventory_option", option, item, inventory) {
        handler.invoke(this)
    }
}

fun inventoryItem(option: String = "*", vararg items: String = arrayOf("*"), inventory: String = "*", block: suspend InventoryOption.() -> Unit) {
    val handler: suspend InventoryOption.(EventDispatcher) -> Unit = {
        block.invoke(this)
    }
    for (item in items) {
        Events.handle("inventory_option", option, item, inventory, handler = handler)
    }
}