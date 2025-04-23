package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class InterfaceOnObject(
    override val character: Player,
    override val target: GameObject,
    val id: String,
    val component: String,
    val index: Int
) : TargetInteraction<Player, GameObject>() {

    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> if (approach) "interface_on_approach_object" else "interface_on_operate_object"
        1 -> target.id
        2 -> id
        3 -> component
        else -> null
    }
}

fun interfaceOnObjectOperate(id: String = "*", component: String = "*", obj: String = "*", arrive: Boolean = true, handler: suspend InterfaceOnObject.() -> Unit) {
    Events.handle<InterfaceOnObject>("interface_on_operate_object", obj, id, component) {
        if (arrive) {
            arriveDelay()
        }
        handler.invoke(this)
    }
}

fun interfaceOnObjectApproach(id: String = "*", component: String = "*", obj: String = "*", handler: suspend InterfaceOnObject.() -> Unit) {
    Events.handle<InterfaceOnObject>("interface_on_approach_object", obj, id, component) {
        handler.invoke(this)
    }
}