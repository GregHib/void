package world.gregs.voidps.world.activity.skill

import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

data class ItemUsedOnItem(val def: ItemOnItemDefinition) : Event {
    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "item_used_on_item"
        1 -> def.skill
        else -> null
    }
}