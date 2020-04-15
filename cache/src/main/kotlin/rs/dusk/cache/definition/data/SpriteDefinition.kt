package rs.dusk.cache.definition.data

import rs.dusk.cache.Definition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
@Suppress("ArrayInDataClass")
data class SpriteDefinition(
    override var id: Int = -1,
    var sprites: Array<IndexedSprite?> = arrayOfNulls(0)
) : Definition