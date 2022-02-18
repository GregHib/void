package world.gregs.voidps.engine.map.collision

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
            else -> land
        }
    }

    fun get(def: NPCDefinition) = when {
        def.name == "Fishing spot" -> shore
        def["swim", false] -> water
        def["fly", false] -> sky
        else -> character
    }
}