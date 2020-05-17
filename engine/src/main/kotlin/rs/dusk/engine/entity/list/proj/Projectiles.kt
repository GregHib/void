package rs.dusk.engine.entity.list.proj

import rs.dusk.engine.entity.list.SimpleList
import rs.dusk.engine.model.entity.proj.Projectile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
data class Projectiles(override val delegate: HashMap<Int, MutableSet<Projectile>> = hashMapOf()) :
    SimpleList<Projectile>