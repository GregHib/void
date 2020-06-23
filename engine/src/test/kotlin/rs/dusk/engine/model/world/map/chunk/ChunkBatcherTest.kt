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
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.world.Chunk
import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.engine.script.KoinMock
import rs.dusk.network.rs.codec.game.encode.message.ChunkClearMessage
import rs.dusk.network.rs.codec.game.encode.message.ChunkUpdateMessage

internal class ChunkBatcherTest : KoinMock() {

    private lateinit var batcher: ChunkBatcher

    override val modules = listOf(clientSessionModule)

    @BeforeEach
    fun setup() {
        batcher = spyk(ChunkBatcher())
    }

    @Test
    fun `Subscription sends chunk`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val result = batcher.createSubscription(player)
        val chunkPlane = ChunkPlane(100)
        val message: Message = mockk(relaxed = true)
        val messages = mutableListOf(message)
        // When
        result.invoke(chunkPlane, messages)
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
        val chunkPlane = ChunkPlane(11, 11)
        val chunk = Chunk(10, 10)
        val player: Player = mockk(relaxed = true)
        every { player.viewport.lastLoadChunk } returns chunk
        every { player.viewport.size } returns size
        // When
        val offset = batcher.getChunkOffset(player, chunkPlane)
        // Then
        assertEquals(7, offset.x)
        assertEquals(7, offset.y)
    }

    @Test
    fun `Send chunk update message`() {
        // Given
        mockkStatic("rs.dusk.engine.client.session.ClientSessionsKt")
        val player: Player = mockk(relaxed = true)
        val chunkPlane = ChunkPlane(11, 11, 1)
        every { batcher.getChunkOffset(player, chunkPlane) } returns Chunk(7, 7)
        val sessions: Sessions = declareMock {
            every { send(player, ChunkUpdateMessage::class, any()) } just Runs
        }
        // When
        batcher.sendChunk(player, chunkPlane)
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
        val chunkPlane = ChunkPlane(11, 11, 1)
        every { batcher.getChunkOffset(player, chunkPlane) } returns Chunk(7, 7)
        val sessions: Sessions = declareMock {
            every { send(player, ChunkClearMessage::class, any()) } just Runs
        }
        // When
        batcher.sendChunkClear(player, chunkPlane)
        // Then
        verify {
            sessions.send(player, ChunkClearMessage::class, ChunkClearMessage(7, 7, 1))
        }
    }

    @Test
    fun `Subscribe sends additions`() {
        // Given
        val subscription: (ChunkPlane, List<Message>) -> Unit = mockk(relaxed = true)
        val subscribers = mutableSetOf<(ChunkPlane, MutableList<Message>) -> Unit>()
        val messages = mutableListOf<Message>()
        val player: Player = mockk(relaxed = true)
        val chunkPlane = ChunkPlane(11, 11, 1)
        batcher.creation[chunkPlane] = messages
        every { batcher.getSubscription(player) } returns subscription
        every { batcher.getSubscribers(chunkPlane) } returns subscribers
        // When
        batcher.subscribe(player, chunkPlane)
        // Then
        verify {
            subscription.invoke(chunkPlane, messages)
        }
        assertTrue(subscribers.contains(subscription))
    }

    @Test
    fun `Unsubscribe removes subscription`() {
        // Given
        val subscription: (ChunkPlane, List<Message>) -> Unit = mockk(relaxed = true)
        val subscribers = mutableSetOf<(ChunkPlane, MutableList<Message>) -> Unit>()
        val player: Player = mockk(relaxed = true)
        val chunkPlane = ChunkPlane(11, 11, 1)
        subscribers.add(subscription)
        every { batcher.getSubscription(player) } returns subscription
        every { batcher.getSubscribers(chunkPlane) } returns subscribers
        // When
        batcher.unsubscribe(player, chunkPlane)
        // Then
        assertFalse(subscribers.contains(subscription))
    }

    @Test
    fun `Subscription created if non-existing`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val subscription: (ChunkPlane, List<Message>) -> Unit = mockk(relaxed = true)
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
        val chunkPlane = ChunkPlane(11, 11, 1)
        // When
        val result = batcher.getSubscribers(chunkPlane)
        // Then
        assertNotNull(result)
    }

    @Test
    fun `Chunk created for batch`() {
        // Given
        val chunkPlane = ChunkPlane(11, 11, 1)
        // When
        val result = batcher.getBatch(chunkPlane)
        // Then
        assertNotNull(result)
    }

    @Test
    fun `Chunk created for creation`() {
        // Given
        val chunkPlane = ChunkPlane(11, 11, 1)
        // When
        val result = batcher.getCreation(chunkPlane)
        // Then
        assertNotNull(result)
    }

    @Test
    fun `Tick notifies all subscribers and clears all batches`() {
        // Given
        val subscription: (ChunkPlane, List<Message>) -> Unit = mockk(relaxed = true)
        val subscribers = mutableSetOf<(ChunkPlane, MutableList<Message>) -> Unit>()
        subscribers.add(subscription)
        val chunkPlane = ChunkPlane(11, 11, 1)
        val message: Message = mockk(relaxed = true)
        val messages = mutableListOf(message)
        batcher.batches[chunkPlane] = messages
        batcher.subscribers[chunkPlane] = subscribers
        // When
        batcher.tick()
        // Then
        verify { subscription.invoke(chunkPlane, messages) }
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `Add message to batch`() {
        // Given
        val chunkPlane = ChunkPlane(11, 11, 1)
        val message: Message = mockk(relaxed = true)
        val batch = mutableListOf<Message>()
        every { batcher.getBatch(chunkPlane) } returns batch
        // When
        batcher.update(chunkPlane, message)
        // Then
        assertTrue(batch.contains(message))
    }

    @Test
    fun `Add to creation batch`() {
        // Given
        val chunkPlane = ChunkPlane(11, 11, 1)
        val message: Message = mockk(relaxed = true)
        val list = mutableListOf<Message>()
        every { batcher.getCreation(chunkPlane) } returns list
        // When
        batcher.addCreation(chunkPlane, message)
        // Then
        assertTrue(list.contains(message))
    }

    @Test
    fun `Remove form creation batch`() {
        // Given
        val chunkPlane = ChunkPlane(11, 11, 1)
        val message: Message = mockk(relaxed = true)
        val list = mutableListOf<Message>()
        list.add(message)
        every { batcher.getCreation(chunkPlane) } returns list
        // When
        batcher.removeCreation(chunkPlane, message)
        // Then
        assertFalse(list.contains(message))
    }
}