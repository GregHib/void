package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player

data class ObjectOption(
    override val player: Player,
    val obj: GameObject,
    val def: ObjectDefinition,
    val option: String
) : Interaction()