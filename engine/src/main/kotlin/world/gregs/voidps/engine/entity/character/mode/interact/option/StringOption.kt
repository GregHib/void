package world.gregs.voidps.engine.entity.character.mode.interact.option

import world.gregs.voidps.engine.entity.character.mode.interact.Approach
import world.gregs.voidps.engine.entity.character.mode.interact.Operate

@JvmInline
value class StringOption(val option: String) : Option

val Approach<*>.option: String
    get() = (optionData as? StringOption)?.option ?: ""

val Operate<*>.option: String
    get() = (optionData as? StringOption)?.option ?: ""