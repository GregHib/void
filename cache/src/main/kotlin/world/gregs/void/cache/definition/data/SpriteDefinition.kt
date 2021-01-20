package world.gregs.void.cache.definition.data

import world.gregs.void.cache.Definition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
@Suppress("ArrayInDataClass")
data class SpriteDefinition(
    override var id: Int = -1,
    var sprites: Array<IndexedSprite>? = null
) : Definition