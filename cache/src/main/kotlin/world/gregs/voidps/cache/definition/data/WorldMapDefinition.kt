package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import java.util.*

data class WorldMapDefinition(
    override var id: Int = -1,
    var map: String = "",
    var name: String = "",
    var position: Int = -1,
    var anInt9542: Int = -1,
    var static: Boolean = false,
    var anInt9547: Int = -1,
    var sections: LinkedList<WorldMapSection>? = null,
    var minX: Int = 12800,
    var minY: Int = 12800,
    var maxX: Int = 0,
    var maxY: Int = 0,
) : Definition
