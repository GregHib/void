package rs.dusk.cache.config.data

import rs.dusk.cache.Definition
import rs.dusk.cache.definition.Details

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
@Suppress("ArrayInDataClass")
data class ItemContainerDefinition(
    override var id: Int = -1,
    var length: Int = 0,
    var ids: IntArray? = null,
    var amounts: IntArray? = null,
    override var details: Map<String, Any> = emptyMap()
) : Definition, Details