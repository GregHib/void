package rs.dusk.cache.definition.data

import rs.dusk.cache.Definition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
@Suppress("ArrayInDataClass")
data class InterfaceDefinition(
    override var id: Int = -1,
    var components: Array<InterfaceComponentDefinition>? = null
) : Definition