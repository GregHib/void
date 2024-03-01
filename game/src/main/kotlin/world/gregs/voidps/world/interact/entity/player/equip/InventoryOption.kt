package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class InventoryOption(
    override val character: Character,
    val inventory: String,
    val item: Item,
    val slot: Int,
    val option: String
) : Interaction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override fun size() = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "inventory_option"
        1 -> option
        2 -> item.id
        3 -> inventory
        else -> ""
    }
}

fun inventoryOptions(vararg options: String = arrayOf("*"), item: String = "*", inventory: String = "*", continueOn: Boolean = false, block: suspend InventoryOption.() -> Unit) {
    val handler: suspend InventoryOption.(EventDispatcher) -> Unit = {
        block.invoke(this)
    }
    for (option in options) {
        Events.handle("inventory_option", option, item, inventory, skipSelf = continueOn, block = handler)
    }
}

fun inventoryOption(option: String = "*", inventory: String = "*", continueOn: Boolean = false, block: suspend InventoryOption.() -> Unit) {
    Events.handle<InventoryOption>("inventory_option", option, "*", inventory, skipSelf = continueOn) {
        block.invoke(this)
    }
}

fun inventoryItem(option: String = "*", item: String = "*", inventory: String = "*", continueOn: Boolean = false, block: suspend InventoryOption.() -> Unit) {
    Events.handle<InventoryOption>("inventory_option", option, item, inventory, skipSelf = continueOn) {
        block.invoke(this)
    }
}