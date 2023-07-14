package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.ObjectTargetContext
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction

data class ObjectOption(
    override val character: Character,
    override val target: GameObject,
    val def: ObjectDefinition,
    val option: String
) : Interaction(), ObjectTargetContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}