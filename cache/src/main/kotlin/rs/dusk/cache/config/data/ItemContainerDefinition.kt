package rs.dusk.cache.config.data

import rs.dusk.cache.Definition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
@Suppress("ArrayInDataClass")
data class ItemContainerDefinition(
    override var id: Int = -1,
    var length: Int = 0,
    var ids: IntArray? = null,
    var amounts: IntArray? = null
) : Definition