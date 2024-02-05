package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class InterfaceSwitch(
    val id: String,
    val component: String,
    val fromItem: Item,
    val fromSlot: Int,
    val fromInventory: String,
    val toId: String,
    val toComponent: String,
    val toItem: Item,
    val toSlot: Int,
    val toInventory: String
) : Event

fun interfaceSwap(id: String, component: String = "*", block: suspend InterfaceSwitch.(Player) -> Unit) {
    on<InterfaceSwitch>({ wildcardEquals(this.id, id) && this.id == this.toId && wildcardEquals(component, this.component) && this.component == this.toComponent }) { player: Player ->
        block.invoke(this, player)
    }
}

fun interfaceSwap(id: String = "*", fromComponent: String = "*", toComponent: String = "*", block: suspend InterfaceSwitch.(Player) -> Unit) {
    on<InterfaceSwitch>({ wildcardEquals(id, this.id) && this.id == this.toId && wildcardEquals(fromComponent, component) && wildcardEquals(toComponent, this.toComponent) }) { player: Player ->
        block.invoke(this, player)
    }
}