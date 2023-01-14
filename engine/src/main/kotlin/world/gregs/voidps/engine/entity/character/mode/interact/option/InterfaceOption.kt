package world.gregs.voidps.engine.entity.character.mode.interact.option

import world.gregs.voidps.engine.entity.character.mode.interact.Approach
import world.gregs.voidps.engine.entity.character.mode.interact.Operate
import world.gregs.voidps.engine.entity.item.Item

class InterfaceOption(
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val container: String
) : Option


val Approach<*>.id: String
    get() = (optionData as? InterfaceOption)?.id ?: ""

val Operate<*>.id: String
    get() = (optionData as? InterfaceOption)?.id ?: ""

val Approach<*>.component: String
    get() = (optionData as? InterfaceOption)?.component ?: ""

val Operate<*>.component: String
    get() = (optionData as? InterfaceOption)?.component ?: ""

val Approach<*>.item: Item
    get() = (optionData as? InterfaceOption)?.item ?: Item.EMPTY

val Operate<*>.item: Item
    get() = (optionData as? InterfaceOption)?.item ?: Item.EMPTY

val Approach<*>.itemSlot: Int
    get() = (optionData as? InterfaceOption)?.itemSlot ?: -1

val Operate<*>.itemSlot: Int
    get() = (optionData as? InterfaceOption)?.itemSlot ?: -1

val Approach<*>.container: String
    get() = (optionData as? InterfaceOption)?.container ?: ""

val Operate<*>.container: String
    get() = (optionData as? InterfaceOption)?.container ?: ""