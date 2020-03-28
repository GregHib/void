package org.redrune.engine.entity.factory

import org.redrune.engine.entity.event.Registered
import org.redrune.engine.entity.model.Projectile
import org.redrune.engine.event.EventBus
import org.redrune.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
class ProjectileFactory {

    private val bus: EventBus by inject()

    fun spawn(index: Int): Projectile {
        val floorItem = Projectile(index)
        bus.emit(Registered(floorItem))
        return floorItem
    }
}