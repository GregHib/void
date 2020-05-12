package rs.dusk.engine.view

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.koin.dsl.module
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.list.MAX_PLAYERS
import rs.dusk.engine.entity.list.PooledMapList
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.model.entity.index.Indexed
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.view.ViewportTask.Companion.LOCAL_NPC_CAP
import rs.dusk.engine.view.ViewportTask.Companion.LOCAL_PLAYER_CAP
import rs.dusk.engine.view.ViewportTask.Companion.NPC_TICK_CAP
import rs.dusk.engine.view.ViewportTask.Companion.PLAYER_TICK_CAP
import rs.dusk.network.rs.codec.game.encode.message.NPCUpdateMessage
import rs.dusk.network.rs.codec.game.encode.message.PlayerUpdateMessage
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
@Suppress("ArrayInDataClass")
data class Viewport(
    val players: TrackingSet<Player> = EntityTrackingSet(PLAYER_TICK_CAP, LOCAL_PLAYER_CAP),
    val npcs: TrackingSet<NPC> = EntityTrackingSet(NPC_TICK_CAP, LOCAL_NPC_CAP),
    val idlePlayers: IntArray = IntArray(MAX_PLAYERS),
    var size: Int = VIEWPORT_SIZES[0],
    val regions: MutableSet<Int> = linkedSetOf(),
    var lastLoadPoint: Tile = Tile(
        0
    )
) {

    val message = PlayerUpdateMessage()
    val npcMessage = NPCUpdateMessage()

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

    companion object {
        val VIEWPORT_SIZES = intArrayOf(104, 120, 136, 168)
    }
}

val viewportModule = module {
    single(createdAtStart = true) { ViewportTask(get()) }
}

class ViewportTask(tasks: EngineTasks) : ParallelEngineTask(tasks, 3) {

    val players: Players by inject()
    val npcs: NPCs by inject()
    val sessions: Sessions by inject()

    override fun run() {
        players.forEach { player ->
            if (!sessions.contains(player)) {
                return@forEach
            }
            defers.add(update(player.tile, players, player.viewport.players, LOCAL_PLAYER_CAP, player))
            defers.add(update(player.tile, npcs, player.viewport.npcs, LOCAL_NPC_CAP, null))
        }
        super.run()
    }

    /**
     * Updates a tracking set quickly, or precisely when local entities exceeds [cap]
     */
    fun <T : Indexed> update(tile: Tile, list: PooledMapList<T>, set: TrackingSet<T>, cap: Int, self: T?) =
        GlobalScope.async {
            set.prep(self)
            val entityCount = nearbyEntityCount(list, tile)
            if (entityCount >= cap) {
                gatherByTile(tile, list, set, self)
            } else {
                gatherByChunk(tile, list, set, self)
            }
        }

    /**
     * Updates [set] precisely for when local entities exceeds maximum stopping at [TrackingSet.maximum]
     */
    fun <T : Indexed> gatherByTile(tile: Tile, list: PooledMapList<T>, set: TrackingSet<T>, self: T?) {
        Spiral.spiral(tile, VIEW_RADIUS) { t ->
            val entities = list[t]
            if (entities != null && !set.track(entities, self)) {
                return
            }
        }
    }

    /**
     * Updates [set] quickly by gathering all entities in local chunks stopping at [TrackingSet.maximum]
     */
    fun <T : Indexed> gatherByChunk(tile: Tile, list: PooledMapList<T>, set: TrackingSet<T>, self: T?) {
        val x = tile.x
        val y = tile.y
        Spiral.spiral(tile.chunk, 2) { chunk ->
            val entities = list[chunk]
            if (entities != null && !set.track(entities, self, x, y)) {
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

    companion object {
        const val PLAYER_TICK_CAP = 40
        const val NPC_TICK_CAP = 40
        const val LOCAL_PLAYER_CAP = 255
        const val LOCAL_NPC_CAP = 255

        // View radius could be controlled per tracking set to give a nicer linear
        // expanding square when loading areas with more than max entities
        const val VIEW_RADIUS = 15
    }
}