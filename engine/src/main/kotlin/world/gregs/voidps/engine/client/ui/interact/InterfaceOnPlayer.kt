package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class InterfaceOnPlayer<C : Character>(
    override val character: C,
    override val target: Player,
    val id: String,
    val component: String,
    val index: Int,
) : TargetInteraction<C, Player>() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> if (approach) "interface_on_approach_player" else "interface_on_operate_player"
        1 -> id
        2 -> component
        else -> null
    }
}

fun interfaceOnPlayerOperate(id: String = "*", component: String = "*", handler: suspend InterfaceOnPlayer<Player>.() -> Unit) {
    Events.handle<InterfaceOnPlayer<Player>>("interface_on_operate_player", id, component) {
        handler.invoke(this)
    }
}

fun interfaceOnPlayerApproach(id: String = "*", component: String = "*", handler: suspend InterfaceOnPlayer<Player>.() -> Unit) {
    Events.handle<InterfaceOnPlayer<Player>>("interface_on_approach_player", id, component) {
        handler.invoke(this)
    }
}
