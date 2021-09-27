package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class SpellDefinition(
    override var id: Int,// TODO could be used for autocast id?
    val damage: Double,
    val experience: Double,
    override var extras: Map<String, Any>
) : Definition, Extra {

    companion object {
        operator fun invoke(map: Map<String, Any>): SpellDefinition {
            val damage = map["damage"] as Double
            val experience = map["experience"] as Double
            return SpellDefinition(-1, damage, experience, map.toMutableMap().apply {
                remove("id")
                remove("damage")
                remove("experience")
            })
        }
    }
}