package world.gregs.voidps.engine.view
/*

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.get
import world.gregs.voidps.engine.client.update.task.SequentialIterator
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerTrackingSet
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.value

internal class ViewportUpdatingIntegrationTest : KoinMock() {

    override val modules = listOf(
        eventModule,
        entityListModule
    )

    lateinit var task: ViewportUpdating

    @BeforeEach
    fun setup() {
        task = spyk(ViewportUpdating(SequentialIterator()))
    }

    @Test
    fun `Crowded area adds in closest first order`() {
        val tile = Tile(15, 15, 0)
        val set = PlayerTrackingSet(
            40
        )
        val players: Players = get()
        for (x in 0..30) {
            for (y in 0..30) {
                val player: Player = mockk(relaxed = true)
                every { player.index } returns players.indexer.obtain()!!
                every { player.compareTo(any()) } answers {
                    player.index.compareTo(arg<Character>(0).index)
                }
                every { player.tile } returns value(Tile(x, y, 0))
                players.add(player)
            }
        }
        // When
        task.gatherByTile(tile, players, set, -1)
        set.update()
        // Then
        assertEquals(386, set.add[0])
        assertEquals(387, set.add[1])
        assertEquals(388, set.add[2])
        assertEquals(389, set.add[3])
        assertEquals(390, set.add[4])
        assertEquals(391, set.add[5])
        assertEquals(417, set.add[6])
    }

    @Test
    fun `Crowded area sends correct amount on second tick`() {
        val radius = 4
        val tile = Tile(radius, radius, 0)
        val set = PlayerTrackingSet(
            40
        )
        val players: Players = get()
        for (x in 0 until radius * 2) {
            for (y in 0 until radius * 2) {
                val player: Player = mockk(relaxed = true)
                every { player.index } returns players.indexer.obtain()!!
                every { player.compareTo(any()) } answers {
                    player.index.compareTo(arg<Character>(0).index)
                }
                every { player.tile } returns value(Tile(x, y, 0))
                players.add(player)
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
        val expected = total - set.tickAddMax
        assertEquals(expected, set.addCount)
    }

}*/
