package world.gregs.voidps.engine.entity.list

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.gfx.Graphics
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.proj.Projectiles
import world.gregs.voidps.engine.entity.sound.Sounds
import world.gregs.voidps.engine.map.Tile

interface EntityList<T : Entity> {

    operator fun get(hash: Int): Set<T?>?

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

const val MAX_PLAYERS = 0x800// 2048
const val MAX_NPCS = 0x8000// 32768

val entityListModule = module {
    single { NPCs(get(), get(), get()) }
    single { Players() }
    single { Objects() }
    single { FloorItems(get(), get(), get(), get()) }
    single { Projectiles() }
    single { Graphics() }
    single { Sounds() }
}