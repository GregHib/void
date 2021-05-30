package world.gregs.voidps.engine.map.chunk

import io.ktor.utils.io.*
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.clearChunk

internal class ChunkBatchesTest : KoinMock() {

    private lateinit var batches: ChunkBatches
    private lateinit var player: Player
    private lateinit var client: Client
    private lateinit var update: ChunkUpdate
    private val chunk = Chunk(0)

    override val modules = listOf(eventModule)

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        client = mockk(relaxed = true)
        update = mockk(relaxed = true)
        every { update.visible(any()) } returns true
        every { update.size } returns 2
        coEvery { update.encode(any()) } coAnswers {
            val channel: ByteWriteChannel = arg(0)
            repeat(3) {
                channel.writeByte(0)
            }
        }
        every { player.client } returns client
        every { client.send(any(), any(), any(), any()) } just Runs
        batches = spyk(ChunkBatches())
        mockkStatic("world.gregs.voidps.network.encode.ChunkEncodersKt")
    }

    @Test
    fun `Subscriptions are sent updates`() {
        // Given
        batches.subscribe(player, chunk)
        batches.update(chunk, update)
        // When
        batches.run()
        // Then
        verify {
            client.send(48, 6, -2, any())
        }
    }

    @Test
    fun `Unsubscribed players aren't sent updates`() {
        // Given
        batches.subscribe(player, chunk)
        batches.update(chunk, update)
        batches.unsubscribe(player, chunk)
        // When
        batches.run()
        // Then
        verify(exactly = 0) {
            client.send(any(), any(), any(), any())
        }
    }

    @Test
    fun `Initial updates are sent on init`() {
        // Given
        batches.addInitial(chunk, update)
        // When
        batches.sendInitial(player, chunk)
        // Then
        verify {
            client.clearChunk(0, 0, 0)
            client.send(48, 6, -2, any())
        }
    }

    @Test
    fun `Initial updates removed aren't sent on init`() {
        // Given
        batches.addInitial(chunk, update)
        batches.removeInitial(chunk, update)
        // When
        batches.sendInitial(player, chunk)
        // Then
        verify {
            client.clearChunk(0, 0, 0)
        }
        verify(exactly = 0) {
            client.send(any(), any(), any(), any())
        }
    }

    /*@Test
    fun `Subscription sends chunk`() {
        // Given
        every { client.updateChunk(any(), any(), any()) } just Runs
        val result = batches.createSubscription(player)
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
        val offset = batches.getChunkOffset(player, chunk)
        // Then
        assertEquals(7, offset.x)
        assertEquals(7, offset.y)
    }

    @Test
    fun `Send chunk update message`() {
        // Given
        every { client.updateChunk(any(), any(), any()) } just Runs
        val chunk = Chunk(11, 11, 1)
        every { batches.getChunkOffset(player, chunk) } returns value(Chunk(7, 7))
        // When
        batches.sendChunk(player, chunk)
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
        every { batches.getChunkOffset(player, chunk) } returns value(Chunk(7, 7))
        // When
        batches.sendChunkClear(player, chunk)
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
        every { batches.getSubscription(player) } returns subscription
        every { batches.getSubscribers(chunk) } returns subscribers
        // When
        batches.unsubscribe(player, chunk)
        // Then
        assertFalse(subscribers.contains(subscription))
    }

    @Test
    fun `Subscription created if non-existing`() {
        // Given
        val subscription: (Chunk, List<(Player) -> Unit>) -> Unit = mockk(relaxed = true)
        every { batches.createSubscription(player) } returns subscription
        // When
        val result = batches.getSubscription(player)
        // Then
        assertNotNull(result)
        verify { batches.createSubscription(player) }
    }

    @Test
    fun `Chunk created for subscribers`() {
        // Given
        val chunk = Chunk(11, 11, 1)
        // When
        val result = batches.getSubscribers(chunk)
        // Then
        assertNotNull(result)
    }

    @Test
    fun `Chunk created for batch`() {
        // Given
        val chunk = Chunk(11, 11, 1)
        // When
        val result = batches.getBatch(chunk)
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
        batches.batches[chunk] = messages
        batches.subscribers[chunk] = subscribers
        // When
        batches.run()
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
        every { batches.getBatch(chunk) } returns batch
        // When
        batches.update(chunk, message)
        // Then
        assertTrue(batch.contains(message))
    }

    @Test
    fun `Add initial`() {
        // Given
        val block: (Player, Chunk, MutableList<(Player) -> Unit>) -> Unit = mockk(relaxed = true)
        // When
        batches.addInitial(block)
        // Then
        assertTrue(batches.initials.contains(block))
    }

    @Test
    fun `Send initial`() {
        // Given
        val block: (Player, Chunk, MutableList<(Player) -> Unit>) -> Unit = mockk(relaxed = true)
        val subscription: (Chunk, List<(Player) -> Unit>) -> Unit = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        batches.initials.add(block)
        every { batches.sendChunkClear(any(), anyValue()) } just Runs
        every { batches.getSubscription(player) } returns subscription
        val chunk = Chunk(10, 10, 1)
        // When
        batches.sendInitial(player, chunk)
        // Then
        verify {
            block.invoke(player, chunk, any())
            subscription.invoke(chunk, any())
        }
    }*/
}