package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
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

fun interfaceOption(filter: InterfaceOption.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend InterfaceOption.(Player) -> Unit) {
    on<InterfaceOption>(filter, priority, block)
}

fun interfaceClick(id: String, component: String, block: suspend InterfaceOption.() -> Unit) {
    on<InterfaceOption>({ wildcardEquals(this.id, id) && wildcardEquals(this.component, component) }) { _: Player ->
        block.invoke(this)
    }
}

fun interfaceClick(id: String, component: String, option: String, block: suspend InterfaceOption.() -> Unit) {
    on<InterfaceOption>({ wildcardEquals(this.id, id) && wildcardEquals(this.component, component) && wildcardEquals(this.option, option) }) { _: Player ->
        block.invoke(this)
    }
}