package rs.dusk.engine.entity.list

import org.koin.dsl.module
import rs.dusk.engine.entity.list.item.FloorItemList
import rs.dusk.engine.entity.list.item.FloorItems
import rs.dusk.engine.entity.list.npc.NPCList
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.obj.ObjectList
import rs.dusk.engine.entity.list.obj.Objects
import rs.dusk.engine.entity.list.player.PlayerList
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.list.proj.ProjectileList
import rs.dusk.engine.entity.list.proj.Projectiles
import rs.dusk.engine.entity.model.Entity
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
interface EntityList<T : Entity> {

    operator fun get(hash: Int): Set<T>?

    operator fun get(tile: Tile) = get(tile.id)

    operator fun get(x: Int, y: Int, plane: Int = 0) = get(Tile(x, y, plane))

    fun add(hash: Int, entity: T): Boolean

    fun add(tile: Tile, entity: T) = add(tile.id, entity)

    fun add(x: Int, y: Int, plane: Int = 0, entity: T) = add(Tile(x, y, plane), entity)

    fun remove(hash: Int, entity: T): Boolean

    fun remove(tile: Tile, entity: T) = remove(tile.id, entity)

    fun remove(x: Int, y: Int, plane: Int = 0, entity: T) = remove(Tile(x, y, plane), entity)

    fun forEach(action: (T) -> Unit)

    operator fun set(hash: Int, entity: T) = add(hash, entity)

    operator fun set(tile: Tile, entity: T) = add(tile, entity)

    operator fun set(x: Int, y: Int, plane: Int = 0, entity: T) = add(x, y, plane, entity)
}

const val MAX_PLAYERS = 2048
const val MAX_NPCS = 10000

@Suppress("USELESS_CAST")
val entityListModule = module {
    single { NPCList() as NPCs }
    single { PlayerList() as Players }
    single { ObjectList() as Objects }
    single { FloorItemList() as FloorItems }
    single { ProjectileList() as Projectiles }
}