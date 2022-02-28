package world.gregs.voidps.engine.client.update

import io.mockk.*
import it.unimi.dsi.fastutil.ints.IntArrayList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.dsl.module
import org.koin.test.mock.declareMock
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
import world.gregs.voidps.engine.tick.Scheduler

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
        declareMock<NPCs> {
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
        every { client.index } returns 1
        every { players.count(tile.chunk) } returns 10
        // When
        task.update(tile, players, set, cap, client)
        // Then
        verifyOrder {
            set.start(client)
            task.gatherByTile(tile, players, set, 1)
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
        every { players.count(tile.chunk) } returns 5
        // When
        task.update(tile, players, set, cap, client)
        // Then
        verifyOrder {
            set.start(client)
            task.gatherByChunk(tile, players, set, client)
        }
    }

    @Test
    fun `Gather by tile tracks by tile spiral`() {
        // Given
        val players: Players = mockk(relaxed = true)
        val set = mockk<PlayerTrackingSet>(relaxed = true)
        val same: Player = mockk(relaxed = true)
        every { same.index } returns 1
        val west: Player = mockk(relaxed = true)
        every { west.index } returns 2
        val northWest: Player = mockk(relaxed = true)
        every { northWest.index } returns 3
        val north: Player = mockk(relaxed = true)
        every { north.index } returns 4
        every { set.track(any<IntArrayList>(), any()) } answers {
            val players: IntArrayList = arg(0)
            players.first() != north.index
        }
        every { players.getDirect(any()) } answers {
            val tile = Tile(arg(0))
            when {
                tile.equals(10, 10) -> IntArrayList.of(same.index)
                tile.equals(9, 10) -> IntArrayList.of(west.index)
                tile.equals(9, 11) -> IntArrayList.of(northWest.index)
                tile.equals(10, 11) -> IntArrayList.of(north.index)
                else -> IntArrayList()
            }
        }
        // When
        task.gatherByTile(Tile(10, 10), players, set, -1)
        // Then
        verifyOrder {
            players.getDirect(Tile(10, 10, 0).id)
            set.track(match { it.size == 1 && it.first() == 1 }, -1)
            players.getDirect(Tile(9, 10, 0).id)
            set.track(match { it.size == 1 && it.first() == 2 }, -1)
            players.getDirect(Tile(9, 11, 0).id)
            set.track(match { it.size == 1 && it.first() == 3 }, -1)
            players.getDirect(Tile(10, 11, 0).id)
            set.track(match { it.size == 1 && it.first() == 4 }, -1)
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