package world.gregs.voidps.engine.view

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.IndexAllocator
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerTrackingSet
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.script.KoinMock

internal class PlayerTrackingSetTest : KoinMock() {
    lateinit var set: PlayerTrackingSet

    override val modules = listOf(eventModule)

    @BeforeEach
    fun setup() {
        set = PlayerTrackingSet(
            tickAddMax = 4,
            localMax = 10,
            radius = 15
        )
    }

    @Test
    fun `Start fills removal set`() {
        // Given
        set.locals[set.lastIndex++] = 1
        set.total = 1
        // When
        set.start(null)
        // Then
        assertTrue(set.remove(1))
        assertEquals(0, set.total)
    }

    @Test
    fun `Start tracks self`() {
        // Given
        val client = Player(index = 1)
        set.locals[set.lastIndex++] = client.index
        // When
        set.start(client)
        // Then
        assertFalse(set.remove(client.index))
        assertEquals(1, set.total)
        assertEquals(0, set.addCount)
    }

    @Test
    fun `Tracking ignores self addition`() {
        // Given
        val client = Player(index = 1)
        // When
        set.track(setOf(client), client)
        // Then
        assertFalse(set.add(client.index))
        assertEquals(0, set.total)
        assertEquals(0, set.addCount)
    }

    @Test
    fun `Update current sets`() {
        // Given
        val toAdd = Player(index = 1)
        val toRemove = Player(index = 2)
        val p1 = Player(index = 3)
        val p2 = Player(index = 4)
        set.locals[set.lastIndex++] = p1.index
        set.locals[set.lastIndex++] = toRemove.index
        set.locals[set.lastIndex++] = p2.index
        set.state.setLocal(p1.index)
        set.state.setLocal(p2.index)
        set.state.setRemoving(toRemove.index)
        set.state.setAdding(toAdd.index)
        set.total = 3
        // When
        val players: Players = mockk()
        val indexer: IndexAllocator = mockk()
        every { players.indexer } returns indexer
        every { indexer.cap } returns 5
        set.update(players)
        // Then
        assertTrue(set.indices.none { set.state.adding(it + 1) })
        assertTrue(set.indices.none { set.state.removing(it + 1) })
        assertTrue(set.locals.contains(toAdd.index))
        assertFalse(set.locals.contains(toRemove.index))
        assertEquals(3, set.total)
    }

    @Test
    fun `Previously unseen entity is added`() {
        // Given
        val player = Player(index = 1)
        val entities = setOf(player)
        // When
        set.track(entities, null)
        // Then
        assertTrue(set.add(player.index))
    }

    @Test
    fun `Tracked and seen entity is not removed`() {
        // Given
        val player = Player(index = 1)
        set.state.setRemoving(player.index)
        val entities = setOf(player)
        // When
        set.track(entities, null)
        // Then
        assertFalse(set.remove(player.index))
        assertFalse(set.add(player.index))
    }

    @Test
    fun `Track exceeding maximum entities`() {
        // Given
        val player = Player(index = 11, tile = Tile(0))
        set.total = 10
        val entities = setOf(player)
        // When
        set.track(entities, null)
        // Then
        assertFalse(set.add(player.index))
    }

    @Test
    fun `Track within view`() {
        // Given
        val player = Player(index = 1, tile = Tile(15, 15, 0))
        val entities = setOf(player)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertTrue(set.add(player.index))
    }

    @Test
    fun `Track outside of view`() {
        // Given
        val player = Player(index = 1, tile = Tile(16, 16, 0))
        val entities = setOf(player)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertFalse(set.add(player.index))
    }

    @Test
    fun `Track within exceeding maximum entities`() {
        // Given
        val player = Player(index = 11, tile = Tile(15, 15, 0))
        set.total = 10
        val entities = setOf(player)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertFalse(set.add(player.index))
    }

    @Test
    fun `Track within exceeding maximum tick entities`() {
        // Given
        val player = Player(index = 5, tile = Tile(15, 15, 0))
        repeat(4) {
            val p = mockk<Player>()
            every { p.index } returns it + 1
            set.track(p, null)
        }
        val entities = setOf(player)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertFalse(set.add(player.index))
    }

    companion object {
        private const val GLOBAL = 0
        private const val LOCAL = 1
        private const val ADDING = 2
        private const val REMOVING = 3
    }
}