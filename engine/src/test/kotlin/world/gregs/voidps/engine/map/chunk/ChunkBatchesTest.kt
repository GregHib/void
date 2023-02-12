package world.gregs.voidps.engine.map.chunk

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import world.gregs.voidps.engine.client.update.batch.ChunkBatches
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.script.KoinMock
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
        player = Player()
        client = mockk(relaxed = true)
        update = mockk(relaxed = true)
        encoders = mockk(relaxed = true)
        mockkStatic("world.gregs.voidps.network.encode.ChunkEncodersKt")
        mockkStatic("world.gregs.voidps.engine.entity.character.player.PlayerVisualsKt")
        every { update.visible(any()) } returns true
        every { update.size } returns 2
        player.client = client
        player["logged_in"] = false
        player.viewport = Viewport()
        player.viewport!!.size = 0
        batches = ChunkBatches(encoders)
    }

    @Test
    fun `Initial updates are sent on login`() {
        // Given
        val chunk = Chunk(2, 2)
        batches.addInitial(chunk, update)
        player.tile = Tile(20, 20)
        player.viewport!!.lastLoadChunk = player.tile.chunk
        player["logged_in"] = true
        // When
        batches.run(player)
        // Then
        verify {
            client.clearChunk(2, 2, 0)
            encoders.encode(client, any(), 2, 2, 0)
        }
    }

    @Test
    fun `Initial updates removed aren't sent on init`() {
        // Given
        batches.addInitial(chunk, update)
        batches.removeInitial(chunk, update)
        player["logged_in"] = true
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
        val chunk = Chunk(11, 11, 1)
        val lastChunk = Chunk(10, 10, 1)
        player.tile = chunk.tile
        player.viewport!!.lastLoadChunk = lastChunk
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
        val chunk = Chunk(11, 11, 1)
        val lastChunk = Chunk(10, 10)
        player["previous_chunk"] = chunk
        player.tile = chunk.tile
        player.viewport!!.lastLoadChunk = lastChunk
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
