package world.gregs.voidps.world.activity.skill.cooking

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

data class Consumable(val item: Item) : Event {
    var cancel = false
}