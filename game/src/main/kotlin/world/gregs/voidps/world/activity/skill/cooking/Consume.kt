package world.gregs.voidps.world.activity.skill.cooking

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

data class Consume(val item: Item, val slot: Int) : Event {
    var cancel = false
}