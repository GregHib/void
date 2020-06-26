package rs.dusk.engine.model.world.map.chunk

import org.koin.dsl.module
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.client.session.send
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.Tick
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.world.Chunk
import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.network.rs.codec.game.encode.message.ChunkClearMessage
import rs.dusk.network.rs.codec.game.encode.message.ChunkUpdateMessage

/**
 * Groups messages by [ChunkPlane] sends to all subscribers
 * Also manages initial batch generation (when a player is in view of a new chunk)
 */
class ChunkBatcher {
    val subscribers = mutableMapOf<ChunkPlane, MutableSet<(ChunkPlane, MutableList<Message>) -> Unit>>()
    val subscriptions = mutableMapOf<Player, (ChunkPlane, List<Message>) -> Unit>()
    val initials = mutableSetOf<(Player, ChunkPlane, MutableList<Message>) -> Unit>()
    val batches = mutableMapOf<ChunkPlane, MutableList<Message>>()

    init {
        Tick.then {
            tick()
        }
    }

    /**
     * Returns the chunk offset for [chunkPlane] relative to [player]'s viewport
     */
    fun getChunkOffset(player: Player, chunkPlane: ChunkPlane): Chunk {
        val viewChunkSize = player.viewport.size shr 4
        val base = player.viewport.lastLoadChunk.minus(viewChunkSize, viewChunkSize)
        return chunkPlane.chunk.minus(base)
    }

    /**
     * Sends [chunkPlane] coordinates to start update to [player]
     */
    fun sendChunk(player: Player, chunkPlane: ChunkPlane) {
        val chunkOffset = getChunkOffset(player, chunkPlane)
        player.send(ChunkUpdateMessage(chunkOffset.x, chunkOffset.y, chunkPlane.plane))
    }

    /**
     * Sends clear message for [chunkPlane] to [player]
     */
    fun sendChunkClear(player: Player, chunkPlane: ChunkPlane) {
        val chunkOffset = getChunkOffset(player, chunkPlane)
        player.send(ChunkClearMessage(chunkOffset.x, chunkOffset.y, chunkPlane.plane))
    }

    /**
     * Subscribes a player to [chunk] for batched updates
     */
    fun subscribe(player: Player, chunk: ChunkPlane): Boolean {
        val subscription = getSubscription(player)
        val subscribers = getSubscribers(chunk)
        return subscribers.add(subscription)
    }

    /**
     * Unsubscribes a player from [chunk]
     */
    fun unsubscribe(player: Player, chunk: ChunkPlane): Boolean {
        val subscription = getSubscription(player)
        val subscribers = getSubscribers(chunk)
        return subscribers.remove(subscription)
    }

    /**
     * Sends the initial batched messages for [chunk] to [player]
     */
    fun sendInitial(player: Player, chunk: ChunkPlane) {
        val subscription = getSubscription(player)
        sendChunkClear(player, chunk)
        val messages = mutableListOf<Message>()
        initials.forEach { init ->
            init(player, chunk, messages)
        }
        subscription.invoke(chunk, messages)
    }

    /**
     * Adds [message] to the batch update for [chunk]
     */
    fun update(chunk: ChunkPlane, message: Message) {
        val batch = getBatch(chunk)
        batch.add(message)
    }

    /**
     * Adds initial messages for a player in a chunk
     */
    fun addInitial(block: (Player, ChunkPlane, MutableList<Message>) -> Unit) {
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
    fun createSubscription(player: Player): (ChunkPlane, List<Message>) -> Unit = { chunk, messages ->
        sendChunk(player, chunk)
        messages.forEach { message ->
            player.send(message)
        }
    }

    fun getSubscription(player: Player) = subscriptions.getOrPut(player) { createSubscription(player) }

    fun getSubscribers(chunk: ChunkPlane) = subscribers.getOrPut(chunk) { mutableSetOf() }

    fun getBatch(chunk: ChunkPlane) = batches.getOrPut(chunk) { mutableListOf() }

}

val batchedChunkModule = module {
    single { ChunkBatcher() }
}