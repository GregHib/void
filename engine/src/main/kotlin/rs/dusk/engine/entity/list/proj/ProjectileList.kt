package rs.dusk.engine.entity.list.proj

import rs.dusk.engine.entity.model.Projectile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class ProjectileList(override val delegate: HashMap<Int, MutableSet<Projectile>> = hashMapOf()) : Projectiles