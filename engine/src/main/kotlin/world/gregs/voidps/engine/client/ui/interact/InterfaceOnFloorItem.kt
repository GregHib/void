package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class InterfaceOnFloorItem<C : Character>(
    override val character: C,
    val floorItem: FloorItem,
    val id: String,
    val component: String,
    val index: Int
) : Interaction<C>() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> if (approach) "interface_approach_floor_item" else "interface_operate_floor_item"
        1 -> floorItem.id
        2 -> id
        3 -> component
        else -> null
    }
}

fun interfaceOnFloorItemOperate(floorItem: String = "*", id: String = "*", component: String = "*", arrive: Boolean = true, handler: suspend InterfaceOnFloorItem<Player>.() -> Unit) {
    Events.handle<InterfaceOnFloorItem<Player>>("interface_approach_floor_item", floorItem, id, component) {
        if (arrive) {
            arriveDelay()
        }
        handler.invoke(this)
    }
}

fun interfaceOnFloorItemApproach(floorItem: String = "*", id: String = "*", component: String = "*", handler: suspend InterfaceOnFloorItem<Player>.() -> Unit) {
    Events.handle<InterfaceOnFloorItem<Player>>("interface_approach_floor_item", floorItem, id, component) {
        handler.invoke(this)
    }
}
