package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class ItemOnNPC(
    override val character: Player,
    override val target: NPC,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : TargetInteraction<Player, NPC>() {

    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> if (approach) "item_on_approach_npc" else "item_on_operate_npc"
        1 -> item.id
        2 -> target.id
        3 -> id
        4 -> component
        else -> null
    }
}

fun itemOnNPCOperate(item: String = "*", npc: String = "*", id: String = "*", component: String = "*", override: Boolean = true, handler: suspend ItemOnNPC.() -> Unit) {
    Events.handle<ItemOnNPC>("item_on_operate_npc", item, npc, id, component, override = override) {
        handler.invoke(this)
    }
}

fun itemOnNPCApproach(item: String = "*", npc: String = "*", id: String = "*", component: String = "*", override: Boolean = true, handler: suspend ItemOnNPC.() -> Unit) {
    Events.handle<ItemOnNPC>("item_on_approach_npc", item, npc, id, component, override = override) {
        handler.invoke(this)
    }
}