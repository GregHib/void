package rs.dusk.engine.model.entity.factory

import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.proj.Projectile
import rs.dusk.engine.model.world.Tile
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
class ProjectileFactory {

    private val bus: EventBus by inject()

    fun spawn(index: Int): Projectile {
        val floorItem = Projectile(index, Tile(0, 0, 0))
        bus.emit(Registered(floorItem))
        return floorItem
    }
}