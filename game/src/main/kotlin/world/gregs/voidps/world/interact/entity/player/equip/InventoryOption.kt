package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class InventoryOption(
    override val character: Character,
    val inventory: String,
    val item: Item,
    val slot: Int,
    val option: String
) : Interaction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}

fun inventoryOptions(vararg options: String = arrayOf("*"), item: String = "*", inventory: String = "*", block: suspend InventoryOption.() -> Unit) {
    for (option in options) {
        on<InventoryOption>({ wildcardEquals(inventory, this.inventory) && wildcardEquals(item, this.item.id) && wildcardEquals(option, this.option) }) {
            block.invoke(this)
        }
    }
}

fun inventoryOption(option: String = "*", inventory: String = "*", block: suspend InventoryOption.() -> Unit) {
    on<InventoryOption>({ wildcardEquals(inventory, this.inventory) && wildcardEquals(option, this.option) }) {
        block.invoke(this)
    }
}

fun inventoryItem(option: String = "*", item: String = "*", inventory: String = "*", priority: Priority = Priority.MEDIUM, block: suspend InventoryOption.() -> Unit) {
    on<InventoryOption>({ wildcardEquals(inventory, this.inventory) && wildcardEquals(item, this.item.id) && wildcardEquals(option, this.option) }, priority) {
        block.invoke(this)
    }
}