package org.redrune.cache.definition.data

import org.redrune.cache.Definition
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
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
    var maxY: Int = 0
) : Definition