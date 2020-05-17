package rs.dusk.engine.model.entity.proj

import rs.dusk.engine.model.entity.list.SimpleList

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
data class Projectiles(override val delegate: HashMap<Int, MutableSet<Projectile>> = hashMapOf()) :
    SimpleList<Projectile>