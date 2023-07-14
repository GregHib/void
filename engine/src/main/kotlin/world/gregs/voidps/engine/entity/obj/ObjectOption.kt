package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.mode.interact.ObjectTargetInteraction
import world.gregs.voidps.engine.entity.character.player.Player

data class ObjectOption(
    override val player: Player,
    override val obj: GameObject,
    val def: ObjectDefinition,
    val option: String
) : ObjectTargetInteraction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}