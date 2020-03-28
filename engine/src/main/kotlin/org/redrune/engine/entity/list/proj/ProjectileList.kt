package org.redrune.engine.entity.list.proj

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import org.redrune.engine.entity.model.Projectile
import org.redrune.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class ProjectileList : Projectiles {
    override val delegate: SetMultimap<Tile, Projectile> = HashMultimap.create()
}