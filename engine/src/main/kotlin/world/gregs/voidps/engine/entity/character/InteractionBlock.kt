package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.character.target.TargetStrategy

data class InteractionBlock<T>(
    val target: T,
    val strategy: TargetStrategy<T>
)