package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

data class InterfaceOption(
    val id: Int,
    val name: String,
    val def: InterfaceDefinition,
    val componentId: Int,
    val component: String,
    val componentDef: InterfaceComponentDefinition,
    val optionId: Int,
    val option: String,
    val item: Item,
    val itemIndex: Int
) : Event
