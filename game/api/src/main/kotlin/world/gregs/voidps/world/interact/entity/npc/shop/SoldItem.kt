package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class SoldItem(val item: Item, val shop: String) : Event {

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "item_sold"
        1 -> item.id
        2 -> shop
        else -> null
    }
}

fun itemSold(item: String = "*", shop: String = "*", handler: suspend SoldItem.(Player) -> Unit) {
    Events.handle("item_sold", item, shop, handler = handler)
}