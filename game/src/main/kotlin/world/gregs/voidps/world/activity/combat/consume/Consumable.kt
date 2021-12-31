package world.gregs.voidps.world.activity.combat.consume

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

data class Consumable(val item: Item) : Event {
    var cancel = false
}