package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.*
import world.gregs.voidps.engine.event.EventField.*
import world.gregs.voidps.engine.event.EventField.Event

data class InterfaceOnNPC(
    override val character: Player,
    override val target: NPC,
    val id: String,
    val component: String,
    val index: Int,
) : TargetInteraction<Player, NPC>() {

    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> if (approach) "interface_on_approach_npc" else "interface_on_operate_npc"
        1 -> target.id
        2 -> id
        3 -> component
        else -> null
    }
}

fun interfaceOnNPCOperate(id: String = "*", component: String = "*", npc: String = "*", handler: suspend InterfaceOnNPC.() -> Unit) {
    Events.handle<InterfaceOnNPC>("interface_on_operate_npc", npc, id, component) {
        handler.invoke(this)
    }
}

fun interfaceOnNPCApproach(id: String = "*", component: String = "*", npc: String = "*", handler: suspend InterfaceOnNPC.() -> Unit) {
    Events.handle<InterfaceOnNPC>("interface_on_approach_npc", npc, id, component) {
        handler.invoke(this)
    }
}
