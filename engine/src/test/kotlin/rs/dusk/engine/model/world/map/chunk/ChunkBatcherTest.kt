package rs.dusk.engine.model.world.map.chunk

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.client.session.Sessions
import rs.dusk.engine.client.session.clientSessionModule
import rs.dusk.engine.client.session.send
import rs.dusk.engine.event.eventBusModule
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.world.Chunk
import rs.dusk.engine.script.KoinMock
import rs.dusk.network.rs.codec.game.encode.message.ChunkClearMessage
import rs.dusk.network.rs.codec.game.encode.message.ChunkUpdateMessage

internal class ChunkBatcherTest : KoinMock() {

    private lateinit var batcher: ChunkBatcher

    override val modules = listOf(clientSessionModule, eventBusModule)

    @BeforeEach
    fun setup() {
        batcher = spyk(ChunkBatcher())
    }

    @Test
    fun `Subscription sends chunk`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val result = batcher.createSubscription(player)
        val chunk = Chunk(100)
        val message: Message = mockk(relaxed = true)
        val messages = mutableListOf(message)
        // When
        result.invoke(chunk, messages)
        // Then
        verify {
            player.send(ChunkUpdateMessage(0, 0, 0))
            player.send(message)
        }
    }

    @Test
    fun `Chunk offset`() {
        // Given
        val size = 104
        val chunk = Chunk(11, 11)
        val lastChunk = Chunk(10, 10)
        val player: Player = mockk(relaxed = true)
        every { player.viewport.lastLoadChunk } returns lastChunk
        every { player.viewport.size } returns size
        // When
        val offset = batcher.getChunkOffset(player, chunk)
        // Then
        assertEquals(7, offset.x)
        assertEquals(7, offset.y)
    }

    @Test
    fun `Send chunk update message`() {
        // Given
        mockkStatic("rs.dusk.engine.client.session.ClientSessionsKt")
        val player: Player = mockk(relaxed = true)
        val chunk = Chunk(11, 11, 1)
        every { batcher.getChunkOffset(player, chunk) } returns Chunk(7, 7)
        val sessions: Sessions = declareMock {
            every { send(player, ChunkUpdateMessage::class, any()) } just Runs
        }
        // When
        batcher.sendChunk(player, chunk)
        // Then
        verify {
            sessions.send(player, ChunkUpdateMessage::class, ChunkUpdateMessage(7, 7, 1))
        }
    }

    @Test
    fun `Send chunk clear message`() {
        // Given
        mockkStatic("rs.dusk.engine.client.session.ClientSessionsKt")
        val player: Player = mockk(relaxed = true)
        val chunk = Chunk(11, 11, 1)
        every { batcher.getChunkOffset(player, chunk) } returns Chunk(7, 7)
        val sessions: Sessions = declareMock {
            every { send(player, ChunkClearMessage::class, any()) } just Runs
        }
        // When
        batcher.sendChunkClear(player, chunk)
        // Then
        verify {
            sessions.send(player, ChunkClearMessage::class, ChunkClearMessage(7, 7, 1))
        }
    }

    @Test
    fun `Unsubscribe removes subscription`() {
        // Given
        val subscription: (Chunk, List<Message>) -> Unit = mockk(relaxed = true)
        val subscribers = mutableSetOf<(Chunk, MutableList<Message>) -> Unit>()
        val player: Player = mockk(relaxed = true)
        val chunk = Chunk(11, 11, 1)
        subscribers.add(subscription)
        every { batcher.getSubscription(player) } returns subscription
        every { batcher.getSubscribers(chunk) } returns subscribers
        // When
        batcher.unsubscribe(player, chunk)
        // Then
        assertFalse(subscribers.contains(subscription))
    }

    @Test
    fun `Subscription created if non-existing`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val subscription: (Chunk, List<Message>) -> Unit = mockk(relaxed = true)
        every { batcher.createSubscription(player) } returns subscription
        // When
        val result = batcher.getSubscription(player)
        // Then
        assertNotNull(result)
        verify { batcher.createSubscription(player) }
    }

    @Test
    fun `Chunk created for subscribers`() {
        // Given
        val chunk = Chunk(11, 11, 1)
        // When
        val result = batcher.getSubscribers(chunk)
        // Then
        assertNotNull(result)
    }

    @Test
    fun `Chunk created for batch`() {
        // Given
        val chunk = Chunk(11, 11, 1)
        // When
        val result = batcher.getBatch(chunk)
        // Then
        assertNotNull(result)
    }

    @Test
    fun `Tick notifies all subscribers and clears all batches`() {
        // Given
        val subscription: (Chunk, List<Message>) -> Unit = mockk(relaxed = true)
        val subscribers = mutableSetOf<(Chunk, MutableList<Message>) -> Unit>()
        subscribers.add(subscription)
        val chunk = Chunk(11, 11, 1)
        val message: Message = mockk(relaxed = true)
        val messages = mutableListOf(message)
        batcher.batches[chunk] = messages
        batcher.subscribers[chunk] = subscribers
        // When
        batcher.tick()
        // Then
        verify { subscription.invoke(chunk, messages) }
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `Add message to batch`() {
        // Given
        val chunk = Chunk(11, 11, 1)
        val message: Message = mockk(relaxed = true)
        val batch = mutableListOf<Message>()
        every { batcher.getBatch(chunk) } returns batch
        // When
        batcher.update(chunk, message)
        // Then
        assertTrue(batch.contains(message))
    }

    @Test
    fun `Add initial`() {
        // Given
        val block: (Player, Chunk, MutableList<Message>) -> Unit = mockk(relaxed = true)
        // When
        batcher.addInitial(block)
        // Then
        assertTrue(batcher.initials.contains(block))
    }

    @Test
    fun `Send initial`() {
        // Given
        val block: (Player, Chunk, MutableList<Message>) -> Unit = mockk(relaxed = true)
        val subscription: (Chunk, List<Message>) -> Unit = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        batcher.initials.add(block)
        every { batcher.sendChunkClear(any(), any()) } just Runs
        every { batcher.getSubscription(player) } returns subscription
        val chunk = Chunk(10, 10, 1)
        // When
        batcher.sendInitial(player, chunk)
        // Then
        verify {
            block.invoke(player, chunk, any())
            subscription.invoke(chunk, any())
        }
    }
}