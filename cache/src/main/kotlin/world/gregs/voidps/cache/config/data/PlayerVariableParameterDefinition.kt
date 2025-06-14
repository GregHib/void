package world.gregs.voidps.cache.config.data

import world.gregs.voidps.cache.Definition

data class PlayerVariableParameterDefinition(
    override var id: Int = -1,
    var type: Int = 0,
) : Definition
