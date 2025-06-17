package world.gregs.voidps.cache.config.data

import world.gregs.voidps.cache.Definition

data class HitSplatDefinition(
    override var id: Int = -1,
    var font: Int = -1,
    var textColour: Int = 16777215,
    var icon: Int = -1,
    var left: Int = -1,
    var middle: Int = -1,
    var right: Int = -1,
    var offsetX: Int = 0,
    var amount: String = "",
    var duration: Int = 70,
    var offsetY: Int = 0,
    var fade: Int = -1,
    var comparisonType: Int = -1,
    var anInt3214: Int = 0,
) : Definition
