package org.redrune.engine.entity.list

import com.google.common.collect.SetMultimap
import org.koin.dsl.module
import org.redrune.engine.entity.list.item.FloorItemList
import org.redrune.engine.entity.list.item.FloorItems
import org.redrune.engine.entity.list.npc.NPCList
import org.redrune.engine.entity.list.npc.NPCs
import org.redrune.engine.entity.list.obj.ObjectList
import org.redrune.engine.entity.list.obj.Objects
import org.redrune.engine.entity.list.player.PlayerList
import org.redrune.engine.entity.list.player.Players
import org.redrune.engine.entity.list.proj.ProjectileList
import org.redrune.engine.entity.list.proj.Projectiles
import org.redrune.engine.entity.model.Entity
import org.redrune.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
interface EntityList<T : Entity> {
    val delegate: SetMultimap<Tile, T>

    operator fun get(tile: Tile): Set<T> {
        return delegate[tile]
    }

    operator fun get(x: Int, y: Int, plane: Int = 0) = get(Tile(x, y, plane))

    operator fun set(tile: Tile, entity: T) {
        delegate.put(tile, entity)
    }

    operator fun set(x: Int, y: Int, plane: Int = 0, entity: T) = set(Tile(x, y, plane), entity)
}

val entityListModule = module {
    single { NPCList() as NPCs }
    single { PlayerList() as Players }
    single { ObjectList() as Objects }
    single { FloorItemList() as FloorItems }
    single { ProjectileList() as Projectiles }
}