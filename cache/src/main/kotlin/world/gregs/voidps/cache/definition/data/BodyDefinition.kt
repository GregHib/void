package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition

/**
 * Equipment Slots Definition
 */
@Suppress("ArrayInDataClass")
data class BodyDefinition(
    override var id: Int = -1,
    var disabledSlots: IntArray = IntArray(0),
    var anInt4506: Int = -1,
    var anInt4504: Int = -1,
    var anIntArray4501: IntArray? = null,
    var anIntArray4507: IntArray? = null
) : Definition