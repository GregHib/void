package world.gregs.voidps.engine.entity.character.target

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.map.Tile

object TargetStrategies {

    val Follow = FollowTargetStrategy
    val Entity = EntityTargetStrategy
    val Default = DefaultTargetStrategy

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(entity: T): TargetStrategy<T> = when (entity) {
        is Tile -> TileTargetStrategy
        is Entity -> EntityTargetStrategy
        else -> DefaultTargetStrategy
    } as TargetStrategy<T>
}