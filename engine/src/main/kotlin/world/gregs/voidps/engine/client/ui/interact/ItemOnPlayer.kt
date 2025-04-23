package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class ItemOnPlayer<C : Character>(
    override val character: C,
    override val target: Player,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : TargetInteraction<C, Player>() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> if (approach) "item_on_approach_player" else "item_on_operate_player"
        1 -> item.id
        else -> null
    }
}

fun itemOnPlayerOperate(item: String = "*", handler: suspend ItemOnPlayer<Player>.() -> Unit) {
    Events.handle<ItemOnPlayer<Player>>("item_on_operate_player", item) {
        handler.invoke(this)
    }
}

fun itemOnPlayerApproach(item: String = "*", handler: suspend ItemOnPlayer<Player>.() -> Unit) {
    Events.handle<ItemOnPlayer<Player>>("item_on_approach_player", item) {
        handler.invoke(this)
    }
}
