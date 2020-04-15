package rs.dusk.engine.entity.list

import com.google.common.collect.SetMultimap
import org.koin.dsl.module
import rs.dusk.engine.entity.list.item.FloorItemList
import rs.dusk.engine.entity.list.npc.NPCList
import rs.dusk.engine.entity.list.obj.ObjectList
import rs.dusk.engine.entity.list.player.PlayerList
import rs.dusk.engine.entity.list.proj.ProjectileList
import rs.dusk.engine.entity.model.Entity
import rs.dusk.engine.model.Tile

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
    single { NPCList() }
    single { PlayerList() }
    single { ObjectList() }
    single { FloorItemList() }
    single { ProjectileList() }
}