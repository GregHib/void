package world.gregs.voidps.engine.entity.character.mode.interact.option

import world.gregs.voidps.engine.entity.character.mode.interact.Approach
import world.gregs.voidps.engine.entity.character.mode.interact.Operate

interface StringOption : Option {
    val option: String
}

@JvmInline
value class StringOptionValue(val option: String) : Option

val Approach<*>.option: String
    get() = (optionData as? StringOption)?.option ?: ""

val Operate<*>.option: String
    get() = (optionData as? StringOption)?.option ?: ""