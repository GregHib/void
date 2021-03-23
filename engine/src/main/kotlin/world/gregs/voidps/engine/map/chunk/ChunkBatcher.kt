package world.gregs.voidps.engine.map.chunk

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.Tick
import world.gregs.voidps.network.encode.clearChunk
import world.gregs.voidps.network.encode.updateChunk

/**
 * Groups messages by [Chunk] sends to all subscribers
 * Also manages initial batch generation (when a player is in view of a new chunk)
 */
class ChunkBatcher {
    val subscribers = mutableMapOf<Chunk, MutableSet<(Chunk, MutableList<(Player) -> Unit>) -> Unit>>()
    val subscriptions = mutableMapOf<Player, (Chunk, List<(Player) -> Unit>) -> Unit>()
    val initials = mutableSetOf<(Player, Chunk, MutableList<(Player) -> Unit>) -> Unit>()
    val batches = mutableMapOf<Chunk, MutableList<(Player) -> Unit>>()

    init {
        on<World, Tick> {
            tick()
        }
    }

    /**
     * Returns the chunk offset for [chunk] relative to [player]'s viewport
     */
    fun getChunkOffset(player: Player, chunk: Chunk): Chunk {
        val viewChunkSize = player.viewport.tileSize shr 4
        val base = player.viewport.lastLoadChunk.minus(viewChunkSize, viewChunkSize)
        return chunk.minus(base)
    }

    /**
     * Sends [chunk] coordinates to start update to [player]
     */
    fun sendChunk(player: Player, chunk: Chunk) {
        val chunkOffset = getChunkOffset(player, chunk)
        player.client?.updateChunk(chunkOffset.x, chunkOffset.y, chunk.plane)
    }

    /**
     * Sends clear message for [chunk] to [player]
     */
    fun sendChunkClear(player: Player, chunk: Chunk) {
        val chunkOffset = getChunkOffset(player, chunk)
        player.client?.clearChunk(chunkOffset.x, chunkOffset.y, chunk.plane)
    }

    /**
     * Subscribes a player to [chunk] for batched updates
     */
    fun subscribe(player: Player, chunk: Chunk): Boolean {
        val subscription = getSubscription(player)
        val subscribers = getSubscribers(chunk)
        return subscribers.add(subscription)
    }

    /**
     * Unsubscribes a player from [chunk]
     */
    fun unsubscribe(player: Player, chunk: Chunk): Boolean {
        val subscription = getSubscription(player)
        val subscribers = getSubscribers(chunk)
        return subscribers.remove(subscription)
    }

    /**
     * Sends the initial batched messages for [chunk] to [player]
     */
    fun sendInitial(player: Player, chunk: Chunk) {
        val subscription = getSubscription(player)
        sendChunkClear(player, chunk)
        val messages = mutableListOf<(Player) -> Unit>()
        initials.forEach { init ->
            init(player, chunk, messages)
        }
        subscription.invoke(chunk, messages)
    }

    /**
     * Adds [message] to the batch update for [chunk]
     */
    fun update(chunk: Chunk, message: (Player) -> Unit) {
        val batch = getBatch(chunk)
        batch.add(message)
    }

    /**
     * Adds initial messages for a player in a chunk
     */
    fun addInitial(block: (Player, Chunk, MutableList<(Player) -> Unit>) -> Unit) {
        initials.add(block)
    }

    /**
     * Sends all chunk batches to subscribers
     */
    fun tick() {
        batches.forEach { (chunk, messages) ->
            if (messages.isNotEmpty()) {
                subscribers[chunk]?.forEach { subscriber ->
                    subscriber.invoke(chunk, messages)
                }
                messages.clear()
            }
        }
    }

    /**
     * Creates a reusable subscription which sends a batch of messages to a [player]
     */
    fun createSubscription(player: Player): (Chunk, List<(Player) -> Unit>) -> Unit = { chunk, messages ->
        if (player.client != null) {
            sendChunk(player, chunk)
            messages.forEach { message ->
                message.invoke(player)
            }
        }
    }

    fun getSubscription(player: Player) = subscriptions.getOrPut(player) { createSubscription(player) }

    fun getSubscribers(chunk: Chunk) = subscribers.getOrPut(chunk) { mutableSetOf() }

    fun getBatch(chunk: Chunk) = batches.getOrPut(chunk) { mutableListOf() }

}

val batchedChunkModule = module {
    single { ChunkBatcher() }
}