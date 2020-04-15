package rs.dusk.cache.config.data

import rs.dusk.cache.Definition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
data class MapSceneDefinition(
    override var id: Int = -1,
    var sprite: Int = 0,
    var colour: Int = 0,
    var aBoolean1741: Boolean = false
) : Definition