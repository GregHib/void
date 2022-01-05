package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.map.collision.strategy.*

class CollisionStrategyProvider(
    private val noClip: NoCollision,
    private val shore: ShoreCollision,
    private val water: WaterCollision,
    private val sky: SkyCollision,
    private val npc: NPCCollision,
    private val player: PlayerCollision,
    private val definitions: NPCDefinitions
) {
    fun get(character: Character): CollisionStrategy {
        return when {
            character.hasEffect("no_clip") -> noClip
            character is NPC -> get(character.def)
            character is Player && character.hasEffect("transform") -> get(definitions.get(character["transform", ""]))
            else -> player
        }
    }

    private fun get(def: NPCDefinition) = when {
        def.name == "Fishing spot" -> shore
        def["swim", false] -> water
        def["fly", false] -> sky
        else -> npc
    }
}