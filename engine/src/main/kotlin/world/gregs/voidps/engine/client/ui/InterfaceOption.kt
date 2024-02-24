package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class InterfaceOption(
    override val character: Character,
    val id: String,
    val component: String,
    val optionIndex: Int,
    val option: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : Interaction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}

fun interfaceOption(option: String = "*", component: String = "*", id: String, itemSlot: Int = -1, block: suspend InterfaceOption.() -> Unit) {
    on<InterfaceOption>({ wildcardEquals(id, this.id) && wildcardEquals(component, this.component) && wildcardEquals(option, this.option) && (itemSlot == -1 || this.itemSlot == itemSlot) }) {
        block.invoke(this)
    }
}