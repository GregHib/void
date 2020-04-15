package rs.dusk.cache.config.data

import rs.dusk.cache.Definition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
data class UnderlayDefinition(
    override var id: Int = -1,
    var colour: Int = 0,
    var texture: Int = -1,
    var scale: Int = 512,
    var blockShadow: Boolean = true,
    var aBoolean2892: Boolean = true,
    var hue: Int = 0,
    var saturation: Int = 0,
    var lightness: Int = 0,
    var chroma: Int = 0
) : Definition