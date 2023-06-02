package world.gregs.voidps.engine.map.chunk

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItemStorage
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.chunk.ChunkUpdate
import world.gregs.voidps.network.encode.chunk.ObjectAddition
import world.gregs.voidps.network.encode.chunk.ObjectRemoval
import world.gregs.voidps.network.encode.clearChunk
import world.gregs.voidps.network.encode.send
import world.gregs.voidps.network.encode.sendBatch

internal class ChunkBatchUpdatesTest : KoinMock() {

    private lateinit var batches: ChunkBatchUpdates
    private lateinit var player: Player
    private lateinit var client: Client
    private lateinit var objects: Objects
    private lateinit var items: FloorItemStorage
    private lateinit var update: ChunkUpdate

    override val modules = listOf(module {
        single { EventHandlerStore() }
    })

    @BeforeEach
    fun setup() {
        player = Player()
        client = mockk(relaxed = true)
        objects = mockk(relaxed = true)
        update = mockk(relaxed = true)
        items = mockk(relaxed = true)
        mockkStatic("world.gregs.voidps.network.encode.ChunkEncodersKt")
        mockkStatic("world.gregs.voidps.network.encode.ChunkUpdateEncodersKt")
        mockkStatic("world.gregs.voidps.engine.entity.character.player.PlayerVisualsKt")
        every { update.size } returns 2
        player.client = client
        player["logged_in"] = false
        player.viewport = Viewport()
        player.viewport!!.size = 0
        batches = ChunkBatchUpdates(objects)
        batches.floorItems = items
    }

    @Test
    fun `Entering chunk sends clear and initial updates`() {
        // Given
        val chunk = Chunk(2, 2)
        batches.add(chunk, update)
        player.tile = Tile(20, 20)
        val added = GameObject("4321", Tile(20, 21), 10, 0)
        added.def = ObjectDefinition(id = 4321)
        every { objects.getAdded(chunk) } returns setOf(added)
        val removed = GameObject("1234", Tile(21, 20), 10, 0)
        removed.def = ObjectDefinition(id = 1234)
        every { objects.getRemoved(chunk) } returns setOf(removed)
        player["logged_in"] = true
        // When
        batches.run(player)
        // Then
        verify {
            client.clearChunk(2, 2, 0)
            client.send(ObjectRemoval(84, 10, 0))
            client.send(ObjectAddition(4321, 69, 10, 0))
        }
    }

    @Test
    fun `Staying in chunk sends batched updates`() {
        // Given
        val chunk = Chunk(11, 11, 1)
        val lastChunk = Chunk(10, 10)
        player["previous_chunk"] = chunk
        player.tile = chunk.tile
        player.viewport!!.lastLoadChunk = lastChunk
        // Given
        batches.add(chunk, update)
        batches.run()
        // When
        batches.run(player)
        // Then
        verify {
            client.sendBatch(any<ByteArray>(), 7, 7, 1)
        }
    }

    @Test
    fun `Staying in chunk sends individual private updates`() {
        // Given
        val chunk = Chunk(11, 11, 1)
        val lastChunk = Chunk(10, 10, 1)
        player.tile = chunk.tile
        player.index = 123
        player["previous_chunk"] = lastChunk
        every { update.private } returns true
        every { update.visible(player.index) } returns true
        // Given
        batches.add(chunk, update)
        // When
        batches.run(player)
        // Then
        verify {
            client.send(update)
        }
        verify(exactly = 0) {
            client.clearChunk(7, 7, 1)
        }
    }

    @Test
    fun `External private updates are ignored`() {
        // Given
        val chunk = Chunk(11, 11, 1)
        val lastChunk = Chunk(10, 10, 1)
        player.tile = chunk.tile
        player.index = 123
        player["previous_chunk"] = lastChunk
        every { update.private } returns true
        every { update.visible(player.index) } returns false
        // Given
        batches.add(chunk, update)
        // When
        batches.run(player)
        // Then
        verify(exactly = 0) {
            client.send(update)
        }
    }
}
