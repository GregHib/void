package world.gregs.voidps.engine.map.collision

import org.rsmod.pathfinder.collision.CollisionStrategies
import org.rsmod.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.map.collision.strategy.*

class CollisionStrategyProvider(
    private val shore: ShoreCollision,
    private val water: WaterCollision,
    private val sky: SkyCollision,
    private val character: CharacterCollision,
    private val land: LandCollision
) {
    fun get(character: Character): CollisionStrategy {
        return when (character) {
            is NPC -> get(character.def)
            else -> CollisionStrategies.Normal
        }
    }

    fun get(def: NPCDefinition) = when {
        def.name == "Fishing spot" -> CollisionStrategies.Swim// FIXME
        def["swim", false] -> CollisionStrategies.Swim
        def["fly", false] -> CollisionStrategies.Fly
        else -> CollisionStrategies.Normal
    }
}