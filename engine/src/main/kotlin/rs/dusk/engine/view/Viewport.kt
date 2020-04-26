package rs.dusk.engine.view

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.koin.dsl.module
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.MAX_PLAYERS
import rs.dusk.engine.entity.list.PooledMapList
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.Entity
import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.model.Tile
import rs.dusk.engine.view.ViewportTask.Companion.LOCAL_NPC_CAP
import rs.dusk.engine.view.ViewportTask.Companion.LOCAL_PLAYER_CAP
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
@Suppress("ArrayInDataClass")
data class Viewport(
    val players: TrackingSet<Player> = TrackingSet(LOCAL_PLAYER_CAP),
    val npcs: TrackingSet<NPC> = TrackingSet(LOCAL_NPC_CAP),
    val idlePlayers: IntArray = IntArray(MAX_PLAYERS)
) {

    fun isActive(index: Int) = idlePlayers[index] and 0x1 == 0

    fun isIdle(index: Int) = idlePlayers[index] and 0x1 != 0

    fun setIdle(index: Int) {
        idlePlayers[index] = idlePlayers[index] or 2
    }

    fun shift() {
        for (index in idlePlayers.indices) {
            idlePlayers[index] = idlePlayers[index] shr 1
        }
    }
}

val viewportModule = module {
    single(createdAtStart = true) { ViewportTask(get()) }
}

class ViewportTask(tasks: EngineTasks) : ParallelEngineTask(tasks, 3) {

    val players: Players by inject()
    val npcs: NPCs by inject()

    override fun run() {
        players.forEach { player ->
            defers.add(update(player.tile, players, player.viewport.players, LOCAL_PLAYER_CAP))
            defers.add(update(player.tile, npcs, player.viewport.npcs, LOCAL_NPC_CAP))
        }
        super.run()
    }

    companion object {
        /**
         * Updates a tracking set quickly, or precisely when local entities exceeds [cap]
         */
        fun <T : Entity> update(tile: Tile, list: PooledMapList<T>, set: TrackingSet<T>, cap: Int) = GlobalScope.async {
            set.prep()
            val entityCount = nearbyEntityCount(list, tile)
            if (entityCount >= cap) {
                gatherByTile(tile, list, set)
            } else {
                gatherByChunk(tile, list, set)
            }
        }

        /**
         * Updates [set] precisely for when local entities exceeds maximum stopping at [TrackingSet.maximum]
         */
        fun <T : Entity> gatherByTile(tile: Tile, list: PooledMapList<T>, set: TrackingSet<T>) {
            Spiral.spiral(tile, VIEW_RADIUS) { t ->
                val p = list[t]
                if (p != null && !set.track(p)) {
                    return
                }
            }
        }

        /**
         * Updates [set] quickly by gathering all entities in local chunks stopping at [TrackingSet.maximum]
         */
        fun <T : Entity> gatherByChunk(tile: Tile, list: PooledMapList<T>, set: TrackingSet<T>) {
            val x = tile.x
            val y = tile.y
            Spiral.spiral(tile.chunk, 2) { chunk ->
                val entities = list[chunk]
                if (entities != null && !set.track(entities, x, y)) {
                    return
                }
            }
        }

        /**
         * Total entities within radius of two chunks
         */
        fun nearbyEntityCount(list: PooledMapList<*>, tile: Tile): Int {
            var total = 0
            Spiral.spiral(tile.chunk, 2) { chunk ->
                val entities = list[chunk]
                if (entities != null) {
                    total += entities.size
                }
            }
            return total
        }

        const val LOCAL_PLAYER_CAP = 255
        const val LOCAL_NPC_CAP = 255

        // View radius could be controlled per tracking set to give a nicer linear
        // expanding square when loading areas with more than max entities
        const val VIEW_RADIUS = 15
    }
}