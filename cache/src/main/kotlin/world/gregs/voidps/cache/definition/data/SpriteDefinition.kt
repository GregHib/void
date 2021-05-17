package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition

@Suppress("ArrayInDataClass")
data class SpriteDefinition(
    override var id: Int = -1,
    var sprites: Array<IndexedSprite>? = null
) : Definition