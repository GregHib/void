package world.gregs.voidps.engine.map.instance

import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.module.Module
import org.koin.dsl.module
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile

internal class InstancesTest : KoinMock() {

    override val modules: List<Module> = listOf(
        module {
            single { mockk<DynamicZones>(relaxed = true) }
        },
    )

    private val players = mutableListOf<Player>()

    @BeforeEach
    fun setup() {
        GameLoop.tick = 0
        Instances.reset()
    }

    @AfterEach
    fun teardown() {
        for (player in players) {
            Players.remove(player)
        }
    }

    @Test
    fun `Small instances starting at 101, 1`() {
        // When
        val first = Instances.small()
        val second = Instances.small()
        // Then
        assertEquals(101, first.x)
        assertEquals(1, first.y)
        assertEquals(101, second.x)
        assertEquals(4, second.y)
    }

    @Test
    fun `Large instances starting at 101, 83`() {
        // When
        val first = Instances.large()
        val second = Instances.large()
        // Then
        assertEquals(101, first.x)
        assertEquals(83, first.y)
        assertEquals(101, second.x)
        assertEquals(89, second.y)
    }

    @Test
    fun `Freed instances are reused`() {
        val first = Instances.small()
        val second = Instances.small()
        GameLoop.tick = Instances.CLEANUP_TICKS + 1
        Instances.cleanup()
        repeat(1375) {
            Instances.small()
        }
        // When
        val one = Instances.small()
        val two = Instances.small()
        // Then
        assertEquals(first, one)
        assertEquals(second, two)
    }

    @Test
    fun `No more instances throws exception`() {
        repeat(1377) {
            Instances.small()
        }
        // Then
        assertThrows<IllegalStateException> {
            Instances.small()
        }
    }

    @Test
    fun `Full instances are cleaned up before allocating`() {
        val first = Instances.small()
        repeat(1376) {
            Instances.small()
        }
        GameLoop.tick = Instances.CLEANUP_TICKS + 1
        // When
        val instance = Instances.small()
        // Then
        assertEquals(first, instance)
    }

    @Test
    fun `Full instances reclaim the longest empty instance waiting on its timeout`() {
        val first = Instances.small(timeout = 30)
        GameLoop.tick = 10
        val second = Instances.small(timeout = 30)
        repeat(1375) {
            Instances.small(timeout = 30)
        }
        GameLoop.tick = 100
        val firstKey = Instances.key(first)
        // When
        val instance = Instances.small()
        // Then
        assertEquals(first, instance)
        assertFalse(Instances.valid(first, firstKey))
        assertTrue(Instances.isInstance(second))
    }

    @Test
    fun `Full instances don't reclaim occupied instances`() {
        repeat(1377) {
            player(Instances.small(timeout = 30).tile.add(10, 10), index = it + 1)
        }
        GameLoop.tick = 1000
        // Then
        assertThrows<IllegalStateException> {
            Instances.small()
        }
    }

    @Test
    fun `Empty instance is kept during grace period`() {
        val instance = Instances.small()
        // When
        GameLoop.tick = Instances.CLEANUP_TICKS
        Instances.cleanup()
        // Then
        assertTrue(Instances.isInstance(instance))
    }

    @Test
    fun `Empty instance is freed after grace period`() {
        val instance = Instances.small()
        // When
        GameLoop.tick = Instances.CLEANUP_TICKS + 1
        Instances.cleanup()
        // Then
        assertFalse(Instances.isInstance(instance))
    }

    @Test
    fun `Occupied instance is kept until players leave`() {
        val instance = Instances.small()
        val player = player(instance.tile.add(10, 10))
        // When
        GameLoop.tick = 1000
        Instances.cleanup()
        // Then
        assertTrue(Instances.isInstance(instance))
        assertTrue(Instances.occupied(instance))

        // When
        Players.remove(player)
        GameLoop.tick = 1000 + Instances.CLEANUP_TICKS + 1
        Instances.cleanup()
        // Then
        assertFalse(Instances.isInstance(instance))
    }

    @Test
    fun `Instance with timeout is kept for timeout minutes after last player leaves`() {
        val instance = Instances.large(timeout = 1) // 100 ticks
        // When
        GameLoop.tick = 100
        Instances.cleanup()
        // Then
        assertTrue(Instances.isInstance(instance))
        assertFalse(Instances.occupied(instance))

        // When
        GameLoop.tick = 101
        Instances.cleanup()
        // Then
        assertFalse(Instances.isInstance(instance))
    }

    @Test
    fun `Run only cleans up every interval`() {
        val instance = Instances.small()
        GameLoop.tick = Instances.CLEANUP_TICKS + 1
        // When
        Instances.run()
        // Then
        assertTrue(Instances.isInstance(instance))
        // When
        GameLoop.tick = Instances.CLEANUP_TICKS * 2
        Instances.run()
        // Then
        assertFalse(Instances.isInstance(instance))
    }

    @Test
    fun `Owner of regions inside and padding of an instance`() {
        val small = Instances.small()
        val large = Instances.large()
        // Then
        assertEquals(small, Instances.owner(small))
        assertEquals(small, Instances.owner(Region(small.x + 1, small.y + 1)))
        assertEquals(small, Instances.owner(Region(small.x, small.y - 1)))
        assertEquals(large, Instances.owner(Region(large.x + 4, large.y + 4)))
        assertEquals(large, Instances.owner(large.tile.add(200, 200)))
        assertNull(Instances.owner(Region(small.x + 3, small.y)))
        assertNull(Instances.owner(Region(50, 50)))
    }

    @Test
    fun `Reallocated instance has a different key`() {
        val instance = Instances.small()
        val key = Instances.key(instance)
        assertTrue(Instances.valid(instance, key))
        GameLoop.tick = Instances.CLEANUP_TICKS + 1
        Instances.cleanup()
        assertFalse(Instances.valid(instance, key))
        repeat(1376) {
            Instances.small()
        }
        // When
        val reused = Instances.small()
        // Then
        assertEquals(instance, reused)
        assertFalse(Instances.valid(reused, key))
        assertTrue(Instances.valid(reused, Instances.key(reused)))
    }

    private fun player(tile: Tile, index: Int = 1): Player {
        val player = Player(index = index, tile = tile)
        Players.add(player)
        players.add(player)
        return player
    }
}
