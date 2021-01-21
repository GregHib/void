package world.gregs.void.cache.config.data

import world.gregs.void.cache.Definition

/**
 * @author GregHib <greg@gregs.world>
 * @since April 07, 2020
 */
data class ClientVariableParameterDefinition(
    override var id: Int = -1,
    var aChar3210: Char = 0.toChar(),
    var anInt3208: Int = 1
) : Definition