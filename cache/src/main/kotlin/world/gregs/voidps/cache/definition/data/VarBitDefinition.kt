package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition

/**
 * @author GregHib <greg@gregs.world>
 * @since April 07, 2020
 */
data class VarBitDefinition(
    override var id: Int = -1,
    var index: Int = 0,
    var leastSignificantBit: Int = 0,
    var mostSignificantBit: Int = 0
) : Definition