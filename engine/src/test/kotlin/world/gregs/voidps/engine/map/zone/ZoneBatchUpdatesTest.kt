package world.gregs.voidps.engine.map.zone

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.protocol.encode.clearZone
import world.gregs.voidps.network.login.protocol.encode.send
import world.gregs.voidps.network.login.protocol.encode.sendBatch
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectAddition
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectRemoval
import world.gregs.voidps.network.login.protocol.encode.zone.ZoneUpdate
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

internal class ZoneBatchUpdatesTest : KoinMock() {

    private lateinit var player: Player
    private lateinit var client: Client
    private lateinit var update: ZoneUpdate

    @BeforeEach
    fun setup() {
        player = Player()
        client = mockk(relaxed = true)
        update = mockk(relaxed = true)
        GameObjects.storeUnused = true
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ZoneEncodersKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ZoneUpdateEncodersKt")
        mockkStatic("world.gregs.voidps.engine.entity.character.player.PlayerVisualsKt")
        every { update.size } returns 2
        player.client = client
        player["logged_in"] = false
        player.viewport = Viewport()
        player.viewport!!.size = 0
    }

    @Test
    fun `Entering zone sends clear and initial updates`() {
        ObjectDefinitions.init(arrayOf(ObjectDefinition(1234)))
        // Given
        val zone = Zone(2, 2)
        ZoneBatchUpdates.add(zone, update)
        player.tile = Tile(20, 20)
        GameObjects.set(id = 1234, x = 21, y = 20, level = 0, shape = ObjectShape.WALL_DECOR_STRAIGHT_NO_OFFSET, rotation = 0, definition = ObjectDefinition.EMPTY)
        ZoneBatchUpdates.register(GameObjects)
        val added = GameObject(4321, Tile(20, 21), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        GameObjects.add(added, collision = false) // Avoid koin
        val removed = GameObject(1234, Tile(21, 20), ObjectShape.WALL_DECOR_STRAIGHT_NO_OFFSET, 0)
        GameObjects.remove(removed, collision = false)
        player["logged_in"] = true
        // When
        ZoneBatchUpdates.run(player)
        // Then
        verify(exactly = 1) {
            client.clearZone(2, 2, 0)
            client.send(ObjectRemoval(tile = 344084, type = ObjectShape.WALL_DECOR_STRAIGHT_NO_OFFSET, rotation = 0))
            client.send(ObjectAddition(tile = 327701, id = 4321, type = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0))
        }
    }

    @Test
    fun `Staying in zone sends batched updates`() {
        // Given
        val zone = Zone(11, 11, 1)
        val lastZone = Zone(10, 10)
        player["previous_zone"] = zone
        player.tile = zone.tile
        player.viewport!!.lastLoadZone = lastZone
        // Given
        ZoneBatchUpdates.add(zone, update)
        ZoneBatchUpdates.run()
        // When
        ZoneBatchUpdates.run(player)
        // Then
        verify {
            client.sendBatch(any<ByteArray>(), 7, 7, 1)
        }
    }

    @Test
    fun `Staying in zone sends individual private updates`() {
        // Given
        val zone = Zone(11, 11, 1)
        val lastZone = Zone(10, 10, 1)
        player.tile = zone.tile
        player["previous_zone"] = lastZone
        every { update.private } returns true
        every { update.visible(player.name) } returns true
        // Given
        ZoneBatchUpdates.add(zone, update)
        // When
        ZoneBatchUpdates.run(player)
        // Then
        verify {
            client.send(update)
        }
        verify(exactly = 0) {
            client.clearZone(7, 7, 1)
        }
    }

    @Test
    fun `External private updates are ignored`() {
        // Given
        val zone = Zone(11, 11, 1)
        val lastZone = Zone(10, 10, 1)
        player.tile = zone.tile
        player["previous_zone"] = lastZone
        every { update.private } returns true
        every { update.visible(player.name) } returns false
        // Given
        ZoneBatchUpdates.add(zone, update)
        // When
        ZoneBatchUpdates.run(player)
        // Then
        verify(exactly = 0) {
            client.send(update)
        }
    }
}
