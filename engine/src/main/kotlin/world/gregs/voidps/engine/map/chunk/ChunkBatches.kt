package world.gregs.voidps.engine.map.chunk

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.io.pool.DefaultPool
import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.map.PooledIdMap
import world.gregs.voidps.engine.map.PooledIntMap
import world.gregs.voidps.network.chunk.ChunkUpdate
import world.gregs.voidps.network.chunk.ChunkUpdateEncoder
import world.gregs.voidps.network.encode.clearChunk

/**
 * Groups messages by [Chunk] to send to all subscribed [Player]s
 * Batched messages are sent and cleared at the end of the tick
 * Initial messages are stored until removed and sent on subscription
 */
class ChunkBatches(
    private val encoders: ChunkUpdateEncoder = ChunkUpdateEncoder()
) : Runnable {
    private val subscribers = PooledIdMap<MutableSet<Player>, Player, Chunk>(
        pool = object : DefaultPool<MutableSet<Player>>(MAX_PLAYERS) {
            override fun produceInstance() = ObjectOpenHashSet<Player>(EXPECTED_PLAYERS_PER_CHUNK)
        }
    )
    private val batches = PooledIdMap<MutableCollection<ChunkUpdate>, ChunkUpdate, Chunk>(
        pool = object : DefaultPool<MutableCollection<ChunkUpdate>>(MAX_PLAYERS) {
            override fun produceInstance() = ObjectLinkedOpenHashSet<ChunkUpdate>(EXPECTED_UPDATES)
        }
    )
    private val initials = PooledIdMap<MutableCollection<ChunkUpdate>, ChunkUpdate, Chunk>(
        pool = object : DefaultPool<MutableCollection<ChunkUpdate>>(MAX_PLAYERS) {
            override fun produceInstance() = ObjectLinkedOpenHashSet<ChunkUpdate>(EXPECTED_UPDATES)
        }
    )
    private val initiated = PooledIntMap(
        pool = object : DefaultPool<MutableSet<Int>>(MAX_PLAYERS) {
            override fun produceInstance() = IntOpenHashSet(EXPECTED_UPDATES)
        }
    )


    /**
     * Returns the chunk offset for [chunk] relative to [player]'s viewport
     */
    private fun getChunkOffset(player: Player, chunk: Chunk): Chunk {
        val viewChunkSize = player.viewport.tileSize shr 4
        val base = player.viewport.lastLoadChunk.minus(viewChunkSize, viewChunkSize)
        return chunk.minus(base)
    }

    /**
     * Subscribes a player to [chunk] for batched updates
     */
    fun subscribe(player: Player, chunk: Chunk): Boolean {
        return subscribers.add(chunk, player)
    }

    /**
     * Unsubscribes a player from [chunk]
     */
    fun unsubscribe(player: Player, chunk: Chunk): Boolean {
        return subscribers.remove(chunk, player)
    }

    /**
     * Adds [message] to the batch update for [chunk]
     */
    fun update(chunk: Chunk, message: ChunkUpdate) {
        batches.add(chunk, message)
    }

    /**
     * Sends the initial batched messages for [chunk] to [player]
     */
    fun sendInitial(player: Player, chunk: Chunk) {
        sendChunkClear(player, chunk)
        val messages = initials[chunk] ?: return
        encode(player, chunk, messages)
        initiated.add(player.index, chunk.id)
    }

    /**
     * Sends clear message for [chunk] to [player]
     */
    private fun sendChunkClear(player: Player, chunk: Chunk) {
        val chunkOffset = getChunkOffset(player, chunk)
        player.client?.clearChunk(chunkOffset.x, chunkOffset.y, chunk.plane)
    }

    /**
     * Adds initial messages for a chunk
     */
    fun addInitial(chunk: Chunk, message: ChunkUpdate) {
        initials.add(chunk, message)
    }

    /**
     * Removes an initial messages for a chunk
     */
    fun removeInitial(chunk: Chunk, message: ChunkUpdate) {
        initials[chunk]?.remove(message)
    }

    /**
     * Sends all chunk batches to subscribers
     */
    override fun run() {
        batches.forEach { (chunk, messages) ->
            if (messages.isEmpty()) {
                return@forEach
            }
            subscribers[chunk]?.forEach { subscriber ->
                if (initiated[subscriber.index]?.contains(chunk) != true) {
                    encode(subscriber, Chunk(chunk), messages)
                }
            }
            messages.clear()
        }
        initiated.clear()
    }

    private fun encode(player: Player, chunk: Chunk, messages: Collection<ChunkUpdate>) {
        val client = player.client ?: return
        val visible = messages.filter { it.visible(player.name) }
        if (visible.isEmpty()) {
            return
        }
        val offset = getChunkOffset(player, chunk)
        encoders.encode(client, messages, offset.x, offset.y, chunk.plane)
    }

    companion object {
        private const val EXPECTED_PLAYERS_PER_CHUNK = 8
        private const val EXPECTED_UPDATES = EXPECTED_PLAYERS_PER_CHUNK * 2
    }

}

val batchedChunkModule = module {
    single { ChunkBatches() }
}