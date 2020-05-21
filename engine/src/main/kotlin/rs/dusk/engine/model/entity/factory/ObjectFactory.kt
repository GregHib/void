package rs.dusk.engine.model.entity.factory

import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.world.Tile
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class ObjectFactory {

    private val bus: EventBus by inject()
    private val decoder: ObjectDecoder by inject()

    fun spawn(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int): Location {
        val definition = decoder.get(id)!!
        val size = Size(definition.sizeX, definition.sizeY)
        val obj = Location(id, Tile(x, y, plane), size, type, rotation)
        bus.emit(Registered(obj))
        return obj
    }
}