package world.gregs.voidps.world.activity.combat.consume

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.CancellableEvent

data class Consumable(val item: Item) : CancellableEvent()