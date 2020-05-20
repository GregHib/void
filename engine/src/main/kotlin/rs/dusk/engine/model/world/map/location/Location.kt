package rs.dusk.engine.model.world.map.location

import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.path.Target
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
data class Location(val id: Int, override val tile: Tile, val type: Int, val rotation: Int) : Target {
    override val size = Size(def.sizeX, def.sizeY)
    val def: ObjectDefinition
        get() = get<ObjectDecoder>().get(id)!!
}