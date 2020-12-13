package rs.dusk.engine.view

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.get
import org.koin.test.mock.declareMock
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.clientSessionModule
import rs.dusk.engine.client.update.task.viewport.ViewportUpdating
import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerTrackingSet
import rs.dusk.engine.entity.character.player.Players
import rs.dusk.engine.entity.list.entityListModule
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.map.Tile
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
internal class ViewportUpdatingIntegrationTest : KoinMock() {

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

    @Test
    fun `Crowded area adds in closest first order`() {
        var index = 1
        val tile = Tile(15, 15, 0)
        val client: Player = mockk(relaxed = true)
        val set = PlayerTrackingSet(
            40,
            ViewportUpdating.LOCAL_PLAYER_CAP
        )
        val players: Players = get()
        for (x in 0..30) {
            for (y in 0..30) {
                val player: Player = mockk(relaxed = true)
                every { player.index } returns index++
                every { player.compareTo(any()) } answers {
                    player.index.compareTo(arg<Character>(0).index)
                }
                every { player.tile } returns Tile(x, y, 0)
                players.add(player)
            }
        }
        declareMock<Sessions> {
            every { contains(any<Player>()) } answers {
                arg<Player>(0) == client
            }
        }
        // When
        task.gatherByTile(tile, players, set, null)
        // Then
        val tiles = set.add.map { it.tile }
        assertEquals(Tile(15, 15), tiles[0])
        assertEquals(Tile(14, 15), tiles[1])
        assertEquals(Tile(14, 16), tiles[2])
        assertEquals(Tile(15, 16), tiles[3])
        assertEquals(Tile(16, 16), tiles[4])
    }

    @Test
    fun `Crowded area sends correct amount on second tick`() {
        var index = 1
        val radius = 4
        val tile = Tile(radius, radius, 0)
        val client: Player = mockk(relaxed = true)
        val set = PlayerTrackingSet(
            40,
            ViewportUpdating.LOCAL_PLAYER_CAP
        )
        val players: Players = get()
        for (x in 0 until radius * 2) {
            for (y in 0 until radius * 2) {
                val player: Player = mockk(relaxed = true)
                every { player.index } returns index++
                every { player.compareTo(any()) } answers {
                    player.index.compareTo(arg<Character>(0).index)
                }
                every { player.tile } returns Tile(x, y, 0)
                players.add(player)
            }
        }
        declareMock<Sessions> {
            every { contains(any<Player>()) } answers {
                arg<Player>(0) == client
            }
        }
        // When
        set.start(null)
        task.gatherByChunk(tile, players, set, null)
        set.update()
        set.start(null)
        task.gatherByChunk(tile, players, set, null)
        // Then
        val total = radius * 2 * radius * 2
        val expected = total - set.tickMax
        assertEquals(expected, set.add.size)
    }

}