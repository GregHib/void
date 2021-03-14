package world.gregs.voidps.engine.data

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.collision.collisionModule
import world.gregs.voidps.engine.path.strat.FollowTargetStrategy
import world.gregs.voidps.engine.path.strat.RectangleTargetStrategy
import world.gregs.voidps.engine.script.KoinMock

/**
 * @author GregHib <greg@gregs.world>
 * @since April 03, 2020
 */
internal class PlayerLoaderTest : KoinMock() {

    override val modules = listOf(eventModule, collisionModule)


    @BeforeEach
    fun setup() {
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.AppearanceKt")
        every { any<Player>().name = any() } just Runs
    }

    @Test
    fun `load strategy`() {
        // Given
        val strategy = mockk<StorageStrategy<Player>>(relaxed = true)
        every { strategy.load("test") } returns mockk(relaxed = true)
        val loader = PlayerLoader(mockk(), mockk(), mockk(), mockk(), strategy)
        // When
        loader.loadPlayer("test", 0)
        // Then
        verify { strategy.load("test") }
    }

    @Test
    fun `load empty`() {
        // Given
        val strategy = mockk<StorageStrategy<Player>>(relaxed = true)
        every { strategy.load("test") } returns null
        val loader = PlayerLoader(mockk(), mockk(), mockk(), mockk(), strategy)
        // When
        val result = loader.loadPlayer("test", 0)
        // Then
        assertEquals(-1, result.id)
        assertEquals(0, result.index)
        assertEquals(0, result.tile.x)
        assertEquals(0, result.tile.y)
        assertEquals(0, result.tile.plane)
        verifyOrder {
            strategy.load("test")
        }
    }

    @Test
    fun `load default`() {
        setProperty("homeX", 100)
        setProperty("homeY", 100)
        setProperty("homePlane", 1)
        // Given
        val strategy = mockk<StorageStrategy<Player>>(relaxed = true)
        every { strategy.load("test") } returns null
        val loader = PlayerLoader(mockk(), mockk(), mockk(), mockk(), strategy)
        // When
        val result = loader.loadPlayer("test", 0)
        // Then
        assertEquals(-1, result.id)
        assertEquals(0, result.index)
        assertEquals(100, result.tile.x)
        assertEquals(100, result.tile.y)
        assertEquals(1, result.tile.plane)
        assert(result.interactTarget is RectangleTargetStrategy)
        assertEquals(FollowTargetStrategy(result), result.followTarget)
        verifyOrder {
            strategy.load("test")
        }
    }
}