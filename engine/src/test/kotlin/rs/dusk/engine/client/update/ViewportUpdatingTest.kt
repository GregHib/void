package rs.dusk.engine.client.update

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.test.mock.declareMock
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.clientSessionModule
import rs.dusk.engine.client.update.task.viewport.ViewportUpdating
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.model.entity.character.CharacterTrackingSet
import rs.dusk.engine.model.entity.character.npc.NPCs
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.player.Players
import rs.dusk.engine.model.entity.list.entityListModule
import rs.dusk.engine.model.map.Tile
import rs.dusk.engine.model.map.chunk.Chunk
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
internal class ViewportUpdatingTest : KoinMock() {

    override val modules = listOf(
        eventModule,
        entityListModule,
        clientSessionModule
    )

    lateinit var task: ViewportUpdating

    @BeforeEach
    fun setup() {
        task = spyk(ViewportUpdating())
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Process players with session`(session: Boolean) {
        // Given
        val player: Player = mockk(relaxed = true)
        declareMock<Players> {
            every { forEach(any()) } answers {
                arg<(Player) -> Unit>(0).invoke(player)
            }
            every { get(any<Chunk>()) } returns null
        }
        declareMock<Sessions> {
            every { contains(player) } returns session
        }
        // When
        task.run()
        // Then
        verify(exactly = if (session) -1 else 0) {
            task.update(any(), any<Players>(), any(), any(), any())
            task.update(any(), any<NPCs>(), any(), any(), any())
        }
    }

    @Test
    fun `Update gathers by tile when exceeds cap`() = runBlocking {
        // Given
        val tile = Tile(0)
        val players: Players = mockk(relaxed = true)
        val set = mockk<CharacterTrackingSet<Player>>(relaxed = true)
        val cap = 10
        val client: Player = mockk(relaxed = true)
        every { task.nearbyEntityCount(players, tile) } returns 10
        // When
        task.update(tile, players, set, cap, client)
        // Then
        verifyOrder {
            set.start(client)
            task.nearbyEntityCount(players, tile)
            task.gatherByTile(tile, players, set, client)
            set.finish()
        }
    }

    @Test
    fun `Update gathers by chunk when under cap`() = runBlocking {
        // Given
        val tile = Tile(0)
        val players: Players = mockk(relaxed = true)
        val set = mockk<CharacterTrackingSet<Player>>(relaxed = true)
        val cap = 10
        val client: Player = mockk(relaxed = true)
        every { task.nearbyEntityCount(players, tile) } returns 5
        // When
        task.update(tile, players, set, cap, client)
        // Then
        verifyOrder {
            set.start(client)
            task.nearbyEntityCount(players, tile)
            task.gatherByChunk(tile, players, set, client)
            set.finish()
        }
    }

    @Test
    fun `Gather by tile tracks by tile spiral`() {
        // Given
        val players: Players = mockk(relaxed = true)
        val set = mockk<CharacterTrackingSet<Player>>(relaxed = true)
        val same: Player = mockk(relaxed = true)
        val west: Player = mockk(relaxed = true)
        val northWest: Player = mockk(relaxed = true)
        val north: Player = mockk(relaxed = true)
        every { set.track(any<Set<Player>>(), any()) } answers {
            val players: Set<Player> = arg(0)
            players.first() != north
        }
        every { players[any<Tile>()] } answers {
            val tile: Tile = arg(0)
            when {
                tile.equals(0, 0) -> setOf(same)
                tile.equals(-1, 0) -> setOf(west)
                tile.equals(-1, 1) -> setOf(northWest)
                tile.equals(0, 1) -> setOf(north)
                else -> null
            }
        }
        // When
        task.gatherByTile(Tile(0), players, set, null)
        // Then
        verifyOrder {
            players[Tile(0, 0, 0)]
            set.track(setOf(same), null)
            players[Tile(-1, 0, 0)]
            set.track(setOf(west), null)
            players[Tile(-1, 1, 0)]
            set.track(setOf(northWest), null)
            players[Tile(0, 1, 0)]
            set.track(setOf(north), null)
        }
    }

    @Test
    fun `Gather by chunk tracks by chunk spiral`() {
        // Given
        val players: Players = mockk(relaxed = true)
        val set = mockk<CharacterTrackingSet<Player>>(relaxed = true)
        val same: Player = mockk(relaxed = true)
        val west: Player = mockk(relaxed = true)
        val northWest: Player = mockk(relaxed = true)
        val north: Player = mockk(relaxed = true)
        every { set.track(any(), any(), any(), any()) } answers {
            val players: Set<Player> = arg(0)
            players.firstOrNull() != north
        }
        every { players[any<Chunk>()] } answers {
            val chunk: Chunk = arg(0)
            when {
                chunk.equals(0, 0, 0) -> setOf(same)
                chunk.equals(-1, 0, 0) -> setOf(west)
                chunk.equals(-1, 1, 0) -> setOf(northWest)
                chunk.equals(0, 1, 0) -> setOf(north)
                else -> null
            }
        }
        // When
        task.gatherByChunk(Tile(0), players, set, null)
        // Then
        verifyOrder {
            players[Chunk(0, 0)]
            set.track(setOf(same), null, 0, 0)
            players[Chunk(-1, 0)]
            set.track(setOf(west), null, 0, 0)
            players[Chunk(-1, 1)]
            set.track(setOf(northWest), null, 0, 0)
            players[Chunk(0, 1)]
            set.track(setOf(north), null, 0, 0)
        }
    }

    @Test
    fun `Nearby entity count`() {
        // Given
        val players: Players = mockk(relaxed = true)
        every { players[any<Chunk>()] } answers {
            val chunk: Chunk = arg(0)
            when {
                chunk.equals(0, 0, 0) -> setOf(mockk(relaxed = true), mockk(relaxed = true))
                chunk.equals(-1, 0, 0) -> setOf(mockk(relaxed = true))
                chunk.equals(-1, 1, 0) -> setOf(mockk(relaxed = true))
                chunk.equals(0, 1, 0) -> setOf(mockk(relaxed = true))
                chunk.equals(0, 0, 1) -> setOf(mockk(relaxed = true))
                else -> null
            }
        }
        // When
        val total = task.nearbyEntityCount(players, Tile(0))
        // Then
        assertEquals(5, total)
    }
}