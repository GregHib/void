package world.gregs.voidps.engine.map.chunk

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.value
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.chunk.ChunkUpdate
import world.gregs.voidps.network.chunk.ChunkUpdateEncoder
import world.gregs.voidps.network.encode.clearChunk

internal class ChunkBatchesTest : KoinMock() {

    private lateinit var batches: ChunkBatches
    private lateinit var encoders: ChunkUpdateEncoder
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
        encoders = mockk(relaxed = true)
        mockkStatic("world.gregs.voidps.network.encode.ChunkEncodersKt")
        every { update.visible(any()) } returns true
        every { update.size } returns 2
        every { player.client } returns client
        batches = ChunkBatches(encoders)
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
            encoders.encode(client, any()/*mutableListOf(update)*/, 0, 0, 0)
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
            encoders.encode(client, any(), 0, 0, 0)
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
            encoders.encode(client, any(), 0, 0, 0)
        }
    }

    @Test
    fun `Chunk offset`() {
        // Given
        val size = 104
        val chunk = Chunk(11, 11, 1)
        val lastChunk = Chunk(10, 10)
        every { player.viewport.lastLoadChunk } returns value(lastChunk)
        every { player.viewport.tileSize } returns size
        // Given
        batches.addInitial(chunk, update)
        batches.removeInitial(chunk, update)
        // When
        batches.sendInitial(player, chunk)
        // Then
        verify {
            client.clearChunk(7, 7, 1)
        }
        verify(exactly = 0) {
            encoders.encode(client, any(), 7, 7, 1)
        }
    }
}