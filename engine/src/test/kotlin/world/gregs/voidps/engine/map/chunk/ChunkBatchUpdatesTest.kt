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
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
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
    private lateinit var update: ChunkUpdate

    override val modules = listOf(module {
        single { EventHandlerStore() }
    })

    @BeforeEach
    fun setup() {
        player = Player()
        client = mockk(relaxed = true)
        update = mockk(relaxed = true)
        mockkStatic("world.gregs.voidps.network.encode.ChunkEncodersKt")
        mockkStatic("world.gregs.voidps.network.encode.ChunkUpdateEncodersKt")
        mockkStatic("world.gregs.voidps.engine.entity.character.player.PlayerVisualsKt")
        every { update.size } returns 2
        player.client = client
        player["logged_in"] = false
        player.viewport = Viewport()
        player.viewport!!.size = 0
        batches = ChunkBatchUpdates()
    }

    @Test
    fun `Entering chunk sends clear and initial updates`() {
        // Given
        val chunk = Chunk(2, 2)
        batches.add(chunk, update)
        player.tile = Tile(20, 20)
        val objects = GameObjects(GameObjectCollision(Collisions()), ChunkBatchUpdates(), mockk(relaxed = true), storeUnused = true)
        objects.set(id = 1234, x = 21, y = 20, plane = 0, shape = ObjectShape.WALL_DECOR_STRAIGHT_NO_OFFSET, rotation = 0, definition = ObjectDefinition.EMPTY)
        batches.register(objects)
        val added = GameObject(4321, Tile(20, 21), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        objects.add(added, collision = false) // Avoid koin
        val removed = GameObject(1234, Tile(21, 20), ObjectShape.WALL_DECOR_STRAIGHT_NO_OFFSET, 0)
        objects.remove(removed, collision = false)
        player["logged_in"] = true
        // When
        batches.run(player)
        // Then
        verify(exactly = 1) {
            client.clearChunk(2, 2, 0)
            client.send(ObjectRemoval(tile = 344084, type = ObjectShape.WALL_DECOR_STRAIGHT_NO_OFFSET, rotation = 0))
            client.send(ObjectAddition(tile = 327701, id = 4321, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
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
        player["previous_chunk"] = lastChunk
        every { update.private } returns true
        every { update.visible(player.name) } returns true
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
        player["previous_chunk"] = lastChunk
        every { update.private } returns true
        every { update.visible(player.name) } returns false
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
