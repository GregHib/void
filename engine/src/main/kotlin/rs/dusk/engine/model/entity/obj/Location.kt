package rs.dusk.engine.model.entity.obj

import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.path.Target
import rs.dusk.utility.get

/**
 * Interactive Object
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Location(
    override val id: Int,
    override var tile: Tile,
    override val size: Size,
    val type: Int,
    val rotation: Int
) : Entity, Target {
    val def: ObjectDefinition
        get() = get<ObjectDecoder>().get(id)!!
}