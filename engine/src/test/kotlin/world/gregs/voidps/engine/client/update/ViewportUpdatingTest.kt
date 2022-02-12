package world.gregs.voidps.engine.client.update

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.dsl.module
import org.koin.test.mock.declareMock
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.anyValue
import world.gregs.voidps.engine.client.update.task.SequentialIterator
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerTrackingSet
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.map.chunk.equals
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.script.KoinMock

internal class ViewportUpdatingTest : KoinMock() {

    override val modules = listOf(
        eventModule,
        entityListModule,
        module {
            single { mockk<NPCDefinitions>(relaxed = true) }
            single { mockk<ItemDefinitions>(relaxed = true) }
            single { mockk<Collisions>(relaxed = true) }
            single { mockk<Scheduler>(relaxed = true) }
            single { mockk<ChunkBatches>(relaxed = true) }
        }
    )

    lateinit var task: ViewportUpdating

    @BeforeEach
    fun setup() {
        task = spyk(ViewportUpdating(SequentialIterator()))
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Process players with session`(session: Boolean) {
        // Given
        val player: Player = mockk(relaxed = true)
        declareMock<Players> {
            every { iterator() } returns mutableListOf(player).iterator()
            every { get(anyValue<Chunk>()) } returns emptySet()
            every { count(any()) } returns 0
        }
        every { player.client } answers {
            if (session) mockk() else null
        }
        // When
        task.run()
        // Then
        verify {
            task.update(anyValue(), any<Players>(), any(), any(), any())
            task.update(anyValue(), any<NPCs>(), any(), any(), any())
        }
    }

    @Test
    fun `Update gathers by tile when exceeds cap`() = runBlocking {
        // Given
        val tile = Tile(0)
        val players: Players = mockk(relaxed = true)
        val set = mockk<PlayerTrackingSet>(relaxed = true)
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
        val set = mockk<PlayerTrackingSet>(relaxed = true)
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
        val set = mockk<PlayerTrackingSet>(relaxed = true)
        val same: Player = mockk(relaxed = true)
        val west: Player = mockk(relaxed = true)
        val northWest: Player = mockk(relaxed = true)
        val north: Player = mockk(relaxed = true)
        every { set.track(any<Set<Player>>(), any()) } answers {
            val players: Set<Player> = arg(0)
            players.first() != north
        }
        every { players[anyValue<Tile>()] } answers {
            val tile = Tile(arg(0))
            when {
                tile.equals(10, 10) -> setOf(same)
                tile.equals(9, 10) -> setOf(west)
                tile.equals(9, 11) -> setOf(northWest)
                tile.equals(10, 11) -> setOf(north)
                else -> emptySet()
            }
        }
        // When
        task.gatherByTile(Tile(10, 10), players, set, null)
        // Then
        verifyOrder {
            players[Tile(10, 10, 0)]
            set.track(setOf(same), null)
            players[Tile(9, 10, 0)]
            set.track(setOf(west), null)
            players[Tile(9, 11, 0)]
            set.track(setOf(northWest), null)
            players[Tile(10, 11, 0)]
            set.track(setOf(north), null)
        }
    }

    @Test
    fun `Gather by chunk tracks by chunk spiral`() {
        // Given
        val players: Players = mockk(relaxed = true)
        val set = mockk<PlayerTrackingSet>(relaxed = true)
        val same: Player = mockk(relaxed = true)
        val west: Player = mockk(relaxed = true)
        val northWest: Player = mockk(relaxed = true)
        val north: Player = mockk(relaxed = true)
        every { set.track(any(), any(), any(), any()) } answers {
            val players: Iterable<Player> = arg(0)
            players.firstOrNull() != north
        }
        every { players[anyValue<Chunk>()] } answers {
            val chunk = Chunk(arg(0))
            when {
                chunk.equals(10, 10, 0) -> setOf(same)
                chunk.equals(9, 10, 0) -> setOf(west)
                chunk.equals(9, 11, 0) -> setOf(northWest)
                chunk.equals(10, 11, 0) -> setOf(north)
                else -> emptySet()
            }
        }
        // When
        task.gatherByChunk(Tile(80, 80), players, set, null)
        // Then
        verifyOrder {
            players[Chunk(10, 10)]
            set.track(setOf(same), null, 80, 80)
            players[Chunk(9, 10)]
            set.track(setOf(west), null, 80, 80)
            players[Chunk(9, 11)]
            set.track(setOf(northWest), null, 80, 80)
            players[Chunk(10, 11)]
            set.track(setOf(north), null, 80, 80)
        }
    }
}