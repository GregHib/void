package rs.dusk.engine.view

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.event.eventBusModule
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerTrackingSet
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 24, 2020
 */
internal class PlayerTrackingSetTest : KoinMock() {
    lateinit var set: PlayerTrackingSet

    override val modules = listOf(eventBusModule)

    @BeforeEach
    fun setup() {
        set = PlayerTrackingSet(
            tickMax = 4,
            maximum = 10,
            radius = 15
        )
    }

    @Test
    fun `Preparation fills removal set`() {
        // Given
        val player = Player(index = 1)
        set.current.add(player)
        set.total = 1
        // When
        set.prep(null)
        // Then
        assert(set.remove.contains(player))
        assertEquals(0, set.total)
    }

    @Test
    fun `Preparation tracks self`() {
        // Given
        val client = Player(index = 1)
        set.remove.add(client)
        // When
        set.prep(client)
        // Then
        assertFalse(set.remove.contains(client))
        assertEquals(1, set.total)
        assertEquals(0, set.add.size)
    }

    @Test
    fun `Tracking tracks self in total`() {
        // Given
        val client = Player(index = 1)
        set.remove.add(client)
        // When
        set.track(setOf(client), client)
        // Then
        assertFalse(set.remove.contains(client))
        assertEquals(1, set.total)
        assertEquals(0, set.add.size)
    }

    @Test
    fun `Tracking ignores self addition`() {
        // Given
        val client = Player(index = 1)
        // When
        set.track(setOf(client), client)
        // Then
        assertFalse(set.add.contains(client))
        assertEquals(0, set.total)
        assertEquals(0, set.add.size)
    }

    @Test
    fun `Update current sets`() {
        // Given
        val toAdd = Player(index = 1)
        val toRemove = Player(index = 2)
        val p1 = Player(index = 3)
        val p2 = Player(index = 4)
        set.current.addAll(listOf(p1, toRemove, p2))
        set.remove.add(toRemove)
        set.add.add(toAdd)
        set.total = 3
        // When
        set.update()
        // Then
        assert(set.add.isEmpty())
        assert(set.remove.isEmpty())
        assert(set.current.contains(toAdd))
        assertFalse(set.current.contains(toRemove))
        assertEquals(3, set.total)
    }

    @Test
    fun `Update removals sets last seen`() {
        // Given
        val p1 = Player(index = 1)
        val p2 = Player(index = 2)
        set.add.add(p1)
        set.remove.add(p2)
        // When
        set.update()
        // Then
        assertFalse(set.lastSeen.containsKey(p1))
        assertTrue(set.lastSeen.containsKey(p2))
    }

    @Test
    fun `Previously unseen entity is added`() {
        // Given
        val player = Player(index = 1)
        val entities = setOf(player)
        // When
        set.track(entities, null)
        // Then
        assertTrue(set.add.contains(player))
    }

    @Test
    fun `Tracked and seen entity is not removed`() {
        // Given
        val player = Player(index = 1)
        set.remove.add(player)
        val entities = setOf(player)
        // When
        set.track(entities, null)
        // Then
        assertFalse(set.remove.contains(player))
        assertFalse(set.add.contains(player))
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
        assertFalse(set.add.contains(player))
    }

    @Test
    fun `Track within view`() {
        // Given
        val player = Player(index = 1, tile = Tile(15, 15, 0))
        val entities = setOf(player)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertTrue(set.add.contains(player))
    }

    @Test
    fun `Track outside of view`() {
        // Given
        val player = Player(index = 1, tile = Tile(16, 16, 0))
        val entities = setOf(player)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertFalse(set.add.contains(player))
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
        assertFalse(set.add.contains(player))
    }

    @Test
    fun `Track within exceeding maximum tick entities`() {
        // Given
        val player = Player(index = 5, tile = Tile(15, 15, 0))
        set.add.addAll(setOf(mockk(), mockk(), mockk(), mockk()))
        val entities = setOf(player)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertFalse(set.add.contains(player))
    }

    @Test
    fun `Clear all entities`() {
        // Given
        set.add.add(Player(index = 0, tile = Tile(0)))
        set.remove.add(Player(index = 0, tile = Tile(0)))
        set.total = 2
        // When
        set.clear()
        // Then
        assert(set.add.isEmpty())
        assert(set.remove.isEmpty())
        assert(set.current.isEmpty())
        assertEquals(0, set.total)
    }
}