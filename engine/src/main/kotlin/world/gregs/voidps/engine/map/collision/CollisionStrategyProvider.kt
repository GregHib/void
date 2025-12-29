package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.cache.definition.type.NPCType
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC

class CollisionStrategyProvider {
    fun get(character: Character): CollisionStrategy = when (character) {
        is NPC -> get(character.def)
        else -> CollisionStrategies.Normal
    }

    fun get(def: NPCType) = when (def["collision", ""]) {
        "sea" -> CollisionStrategies.Blocked
        "indoors" -> CollisionStrategies.Indoors
        "outdoors" -> CollisionStrategies.Outdoors
        "sky" -> CollisionStrategies.LineOfSight
        else -> CollisionStrategies.Normal
    }
}
