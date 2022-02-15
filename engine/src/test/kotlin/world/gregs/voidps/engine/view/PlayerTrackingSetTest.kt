package world.gregs.voidps.engine.view

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerTrackingSet
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.script.KoinMock

internal class PlayerTrackingSetTest : KoinMock() {
    lateinit var set: PlayerTrackingSet

    override val modules = listOf(eventModule)

    @BeforeEach
    fun setup() {
        set = PlayerTrackingSet(
            tickMax = 4,
            maximum = 10,
            radius = 15
        )
    }

    @Test
    fun `Start fills removal set`() {
        // Given
        val player = Player(index = 1)
        set.current.add(player)
        set.total = 1
        // When
        set.start(null)
        // Then
        assert(set.remove.contains(player))
        assertEquals(0, set.total)
    }

    @Test
    fun `Start tracks self`() {
        // Given
        val client = Player(index = 1)
        set.remove.add(client)
        // When
        set.start(client)
        // Then
        assertFalse(set.remove(client.index))
        assertEquals(1, set.total)
        assertEquals(0, set.add.size())
    }

    @Test
    fun `Tracking tracks self in total`() {
        // Given
        val client = Player(index = 1)
        set.remove.add(client)
        set.state[client.index] = REMOVING
        // When
        set.track(setOf(client), client)
        // Then
        assertFalse(set.remove.contains(client))
        assertEquals(1, set.total)
        assertEquals(0, set.add.size())
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
        assertEquals(0, set.add.size())
    }

    @Test
    fun `Update current sets`() {
        // Given
        val toAdd = Player(index = 1)
        val toRemove = Player(index = 2)
        val p1 = Player(index = 3)
        val p2 = Player(index = 4)
        set.current.addAll(listOf(p1, toRemove, p2))
        set.state[p1.index] = LOCAL
        set.state[p2.index] = LOCAL
        set.remove.add(toRemove)
        set.state[toRemove.index] = REMOVING
        set.add.enqueue(toAdd)
        set.state[toAdd.index] = ADDING
        set.total = 3
        // When
        set.update()
        // Then
        assert(set.add.isEmpty)
        assert(set.remove.isEmpty())
        assert(set.current.contains(toAdd))
        assertFalse(set.current.contains(toRemove))
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
        set.remove.add(player)
        set.state[player.index] = REMOVING
        val entities = setOf(player)
        // When
        set.track(entities, null)
        // Then
        assertFalse(set.remove.contains(player))
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
            set.add.enqueue(mockk())
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