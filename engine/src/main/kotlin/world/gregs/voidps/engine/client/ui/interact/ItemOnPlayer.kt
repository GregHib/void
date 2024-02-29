package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetPlayerContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.suspend.arriveDelay

data class ItemOnPlayer(
    override val character: Character,
    override val target: Player,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : Interaction(), TargetPlayerContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override fun size() = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> if (approach) "item_on_approach_player" else "item_on_operate_player"
        1 -> item.id
        2 -> id
        3 -> component
        else -> ""
    }
}

fun itemOnPlayerOperate(item: String = "*", id: String = "*", component: String = "*", arrive: Boolean = true, continueOn: Boolean = false, block: suspend ItemOnPlayer.() -> Unit) {
    Events.handle<Player, ItemOnPlayer>("item_on_operate_player", item, id, component, skipSelf = continueOn) {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
}

fun itemOnPlayerApproach(item: String = "*", id: String = "*", component: String = "*", continueOn: Boolean = false, block: suspend ItemOnPlayer.() -> Unit) {
    Events.handle<Player, ItemOnPlayer>("item_on_approach_player", item, id, component, skipSelf = continueOn) {
        block.invoke(this)
    }
}
