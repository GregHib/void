package world.gregs.voidps.engine.map.chunk

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import world.gregs.voidps.engine.client.update.batch.ChunkBatches
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Delta
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

    override val modules = listOf(module {
        single { EventHandlerStore() }
    })

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        client = mockk(relaxed = true)
        update = mockk(relaxed = true)
        encoders = mockk(relaxed = true)
        mockkStatic("world.gregs.voidps.network.encode.ChunkEncodersKt")
        mockkStatic("world.gregs.voidps.engine.entity.character.player.PlayerVisualExtensionsKt")
        every { update.visible(any()) } returns true
        every { update.size } returns 2
        every { player.client } returns client
        every { player.name } returns "player"
        batches = ChunkBatches(encoders)
    }

    @Test
    fun `Initial updates are sent on init`() {
        // Given
        batches.addInitial(chunk, update)
        // When
        batches.run(player)
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
        batches.run(player)
        // Then
        verify {
            client.clearChunk(0, 0, 0)
        }
        verify(exactly = 0) {
            encoders.encode(client, any(), 0, 0, 0)
        }
    }

    @Test
    fun `Send initial with clear`() {
        // Given
        val size = 104
        val chunk = Chunk(11, 11, 1)
        val lastChunk = Chunk(10, 10)
        every { player.tile } returns chunk.tile
        every { player.viewport!!.lastLoadChunk } returns value(lastChunk)
        every { player.viewport!!.chunkRadius } returns (size shr 4)
        val update2: ChunkUpdate = mockk(relaxed = true)
        // Given
        batches.addInitial(chunk, update)
        batches.update(chunk, update2)
        // When
        batches.run(player)
        // Then
        verify {
            client.clearChunk(7, 7, 1)
            encoders.encode(client, match { it.contains(update) }, 7, 7, 1)
        }
        verify(exactly = 0) {
            encoders.encode(client, match { it.contains(update2) }, 7, 7, 1)
        }
    }

    @Test
    fun `Chunks already sent are updated not cleared`() {
        // Given
        val size = 104
        val chunk = Chunk(11, 11, 1)
        val lastChunk = Chunk(10, 10)
        every { player.movement.delta } returns Delta(0, 0, 0)
        every { player.tile } returns chunk.tile
        every { player.viewport!!.lastLoadChunk } returns value(lastChunk)
        every { player.viewport!!.chunkRadius } returns (size shr 4)
        val update2: ChunkUpdate = mockk(relaxed = true)
        every { update2.visible(any()) } returns true
        // Given
        batches.addInitial(chunk, update)
        batches.update(chunk, update2)
        // When
        batches.run(player)
        // Then
        verify(exactly = 0) {
            client.clearChunk(7, 7, 1)
            encoders.encode(client, match { it.contains(update) }, 7, 7, 1)
        }
        verify {
            encoders.encode(client, match { it.contains(update2) }, 7, 7, 1)
        }
    }
}