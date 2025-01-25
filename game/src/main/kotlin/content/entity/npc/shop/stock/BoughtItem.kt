package content.entity.npc.shop.stock

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class BoughtItem(val item: Item, val shop: String) : Event {

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "item_bought"
        1 -> item.id
        2 -> shop
        else -> null
    }
}

fun itemBought(item: String = "*", shop: String = "*", handler: suspend BoughtItem.(Player) -> Unit) {
    Events.handle("item_bought", item, shop, handler = handler)
}