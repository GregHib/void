package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition

data class VarBitDefinition(
    override var id: Int = -1,
    var index: Int = 0,
    var startBit: Int = 0,
    var endBit: Int = 0,
) : Definition
