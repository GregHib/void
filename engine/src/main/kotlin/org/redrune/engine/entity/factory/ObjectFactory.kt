package org.redrune.engine.entity.factory

import org.redrune.engine.entity.event.Registered
import org.redrune.engine.entity.model.IObject
import org.redrune.engine.event.EventBus
import org.redrune.engine.model.Tile
import org.redrune.utility.inject

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