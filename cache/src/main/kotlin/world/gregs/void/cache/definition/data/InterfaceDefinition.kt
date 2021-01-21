package world.gregs.void.cache.definition.data

import world.gregs.void.cache.Definition

/**
 * @author GregHib <greg@gregs.world>
 * @since April 08, 2020
 */
@Suppress("ArrayInDataClass")
data class InterfaceDefinition(
    override var id: Int = -1,
    var components: Map<Int, InterfaceComponentDefinition>? = null
) : Definition