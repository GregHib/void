package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.collision.CollisionStrategy
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

    fun get(def: NPCDefinition) = when (def.walkMask.toInt()) {
        BLOCKED -> CollisionStrategies.Indoors
        STRAIGHT_LINES -> CollisionStrategies.LineOfSight
        INDOORS -> CollisionStrategies.Normal
        NORMAL -> CollisionStrategies.Normal
        PASS_THROUGH -> CollisionStrategies.Normal
        OUTDOORS -> CollisionStrategies.Blocked
        NO_MOVE -> CollisionStrategies.Blocked
        else -> CollisionStrategies.Blocked
    }

    companion object {
        const val NORMAL = 0
        const val BLOCKED = 1 // Indoors
        const val STRAIGHT_LINES = 2 // Route
        const val INDOORS = 3 // Anywhere
        const val OUTDOORS = 4 // Fishing spot
        const val NO_MOVE = 5 // No move
        const val PASS_THROUGH = 6
        const val WATER = 7 // Water
    }
}