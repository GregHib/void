package rs.dusk.engine.model.entity.factory

import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.obj.IObject
import rs.dusk.engine.model.world.Tile
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class ObjectFactory {

    private val bus: EventBus by inject()

    fun spawn(id: Int, x: Int, y: Int, plane: Int, rotation: Int): IObject {
        val obj = IObject(id, Tile(x, y, plane))
        bus.emit(Registered(obj))
        return obj
    }
}