package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetPlayerContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.suspend.arriveDelay

data class ItemOnPlayer<C: Character>(
    override val character: C,
    override val target: Player,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : Interaction<C>(), TargetPlayerContext<C> {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> if (approach) "item_on_approach_player" else "item_on_operate_player"
        1 -> item.id
        2 -> id
        3 -> component
        else -> null
    }
}

fun itemOnPlayerOperate(item: String = "*", id: String = "*", component: String = "*", arrive: Boolean = true, override: Boolean = true, handler: suspend ItemOnPlayer<Player>.() -> Unit) {
    Events.handle<ItemOnPlayer<Player>>("item_on_operate_player", item, id, component, override = override) {
        if (arrive) {
            arriveDelay()
        }
        handler.invoke(this)
    }
}

fun itemOnPlayerApproach(item: String = "*", id: String = "*", component: String = "*", override: Boolean = true, handler: suspend ItemOnPlayer<Player>.() -> Unit) {
    Events.handle<ItemOnPlayer<Player>>("item_on_approach_player", item, id, component, override = override) {
        handler.invoke(this)
    }
}
