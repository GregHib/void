package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.ui.interact.ObjectInteraction
import world.gregs.voidps.engine.entity.character.player.Player

data class ObjectOption(
    override val player: Player,
    override val obj: GameMapObject,
    val def: ObjectDefinition,
    val option: String
) : ObjectInteraction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}