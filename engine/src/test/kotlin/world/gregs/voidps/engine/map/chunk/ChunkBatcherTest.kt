package world.gregs.voidps.engine.map.chunk

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.anyValue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.value
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.clearChunk
import world.gregs.voidps.network.encode.updateChunk
import kotlin.collections.set

internal class ChunkBatcherTest : KoinMock() {

    private lateinit var batcher: ChunkBatcher
    private lateinit var player: Player
    private lateinit var client: Client

    override val modules = listOf(eventModule)

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        client = mockk(relaxed = true)
        every { player.client } returns client
        batcher = spyk(ChunkBatcher())
        mockkStatic("world.gregs.voidps.network.encode.ChunkEncodersKt")
    }

    @Test
    fun `Subscription sends chunk`() {
        // Given
        every { client.updateChunk(any(), any(), any()) } just Runs
        val result = batcher.createSubscription(player)
        val chunk = Chunk(0)
        val message: (Player) -> Unit = mockk(relaxed = true)
        val messages = mutableListOf(message)
        // When
        result.invoke(chunk, messages)
        // Then
        verify {
            client.updateChunk(0, 0, 0)
            message.invoke(player)
        }
    }

    @Test
    fun `Chunk offset`() {
        // Given
        val size = 104
        val chunk = Chunk(11, 11)
        val lastChunk = Chunk(10, 10)
        every { player.viewport.lastLoadChunk } returns value(lastChunk)
        every { player.viewport.tileSize } returns size
        // When
        val offset = batcher.getChunkOffset(player, chunk)
        // Then
        assertEquals(7, offset.x)
        assertEquals(7, offset.y)
    }

    @Test
    fun `Send chunk update message`() {
        // Given
        every { client.updateChunk(any(), any(), any()) } just Runs
        val chunk = Chunk(11, 11, 1)
        every { batcher.getChunkOffset(player, chunk) } returns value(Chunk(7, 7))
        // When
        batcher.sendChunk(player, chunk)
        // Then
        verify {
            client.updateChunk(7, 7, 1)
        }
    }

    @Test
    fun `Send chunk clear message`() {
        // Given
        every { client.clearChunk(any(), any(), any()) } just Runs
        val chunk = Chunk(11, 11, 1)
        every { batcher.getChunkOffset(player, chunk) } returns value(Chunk(7, 7))
        // When
        batcher.sendChunkClear(player, chunk)
        // Then
        verify {
            client.clearChunk(7, 7, 1)
        }
    }

    @Test
    fun `Unsubscribe removes subscription`() {
        // Given
        val subscription: (Chunk, List<(Player) -> Unit>) -> Unit = mockk(relaxed = true)
        val subscribers = mutableSetOf<(Chunk, MutableList<(Player) -> Unit>) -> Unit>()
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
        val subscription: (Chunk, List<(Player) -> Unit>) -> Unit = mockk(relaxed = true)
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
        val subscription: (Chunk, List<(Player) -> Unit>) -> Unit = mockk(relaxed = true)
        val subscribers = mutableSetOf<(Chunk, MutableList<(Player) -> Unit>) -> Unit>()
        subscribers.add(subscription)
        val chunk = Chunk(11, 11, 1)
        val message: (Player) -> Unit = mockk(relaxed = true)
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
        val message: (Player) -> Unit = mockk(relaxed = true)
        val batch = mutableListOf<(Player) -> Unit>()
        every { batcher.getBatch(chunk) } returns batch
        // When
        batcher.update(chunk, message)
        // Then
        assertTrue(batch.contains(message))
    }

    @Test
    fun `Add initial`() {
        // Given
        val block: (Player, Chunk, MutableList<(Player) -> Unit>) -> Unit = mockk(relaxed = true)
        // When
        batcher.addInitial(block)
        // Then
        assertTrue(batcher.initials.contains(block))
    }

    @Test
    fun `Send initial`() {
        // Given
        val block: (Player, Chunk, MutableList<(Player) -> Unit>) -> Unit = mockk(relaxed = true)
        val subscription: (Chunk, List<(Player) -> Unit>) -> Unit = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        batcher.initials.add(block)
        every { batcher.sendChunkClear(any(), anyValue()) } just Runs
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