package rs.dusk.cache.config.data

import rs.dusk.cache.Definition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
data class ClientVariableParameterDefinition(
    override var id: Int = -1,
    var aChar3210: Char = 0.toChar(),
    var anInt3208: Int = 1
) : Definition