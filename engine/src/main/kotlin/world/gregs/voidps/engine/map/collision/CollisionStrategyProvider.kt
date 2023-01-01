package world.gregs.voidps.engine.map.collision

import org.rsmod.pathfinder.collision.CollisionStrategies
import org.rsmod.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC

class CollisionStrategyProvider {
    fun get(character: Character): CollisionStrategy {
        return when (character) {
            is NPC -> get(character.def)
            else -> CollisionStrategies.Normal
        }
    }

    fun get(def: NPCDefinition) = when {
        def.name == "Fishing spot" -> CollisionStrategies.Swim// FIXME swim != shore
        def["swim", false] -> CollisionStrategies.Swim
        def["fly", false] -> CollisionStrategies.Fly
        else -> CollisionStrategies.Normal
    }
}