package world.gregs.voidps.world.interact.entity.player.combat.consume

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.CancellableEvent

data class Consumable(val item: Item) : CancellableEvent()