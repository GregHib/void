package world.gregs.voidps.world.activity.skill

import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.event.Event

data class ItemUsedOnItem(val def: ItemOnItemDefinition) : Event