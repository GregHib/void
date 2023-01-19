package world.gregs.voidps.engine.entity.character.mode.interact.option

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.mode.interact.Approach
import world.gregs.voidps.engine.entity.character.mode.interact.Operate

data class ObjectDefinitionOption(override val option: String, val def: ObjectDefinition) : StringOption

val Approach<*>.def: ObjectDefinition
    get() = (optionData as? ObjectDefinitionOption)?.def ?: ObjectDefinition.EMPTY

val Operate<*>.def: ObjectDefinition
    get() = (optionData as? ObjectDefinitionOption)?.def ?: ObjectDefinition.EMPTY