package rs.dusk.cache.definition.data

import rs.dusk.cache.Definition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
data class VarBitDefinition(
    override var id: Int = -1,
    var index: Int = 0,
    var leastSignificantBit: Int = 0,
    var mostSignificantBit: Int = 0
) : Definition