package rs.dusk.engine.entity.obj

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.test.mock.declareMock
import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.entity.obj.detail.ObjectDetails
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.path.strat.DecorationTargetStrategy
import rs.dusk.engine.path.strat.EntityTileTargetStrategy
import rs.dusk.engine.path.strat.RectangleTargetStrategy
import rs.dusk.engine.path.strat.WallTargetStrategy
import rs.dusk.engine.script.KoinMock

internal class GameObjectFactoryTest : KoinMock() {

    lateinit var factory: GameObjectFactory

    lateinit var collisions: Collisions

    override val modules = listOf(cacheDefinitionModule)

    @BeforeEach
    fun setup() {
        collisions = mockk(relaxed = true)
        factory = GameObjectFactory(collisions)
        declareMock<ObjectDetails> {
            every { get(any<Int>()) } returns ObjectDefinition(id = 1, blockFlag = 1)
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 9])
    fun `Wall object strategy`(type: Int) {
        // When
        val gameObject = factory.spawn(1, Tile.EMPTY, type, 0)
        // Then
        assertEquals(WallTargetStrategy(collisions, gameObject), gameObject.interactTarget)
    }

    @ParameterizedTest
    @ValueSource(ints = [3, 4, 5, 6, 7, 8])
    fun `Wall decoration object strategy`(type: Int) {
        // When
        val gameObject = factory.spawn(1, Tile.EMPTY, type, 0)
        // Then
        assertEquals(
            DecorationTargetStrategy(collisions, gameObject),
            gameObject.interactTarget
        )
    }

    @ParameterizedTest
    @ValueSource(ints = [10, 11, 22])
    fun `Floor decoration object strategy`(type: Int) {
        // When
        val gameObject = factory.spawn(1, Tile.EMPTY, type, 0)
        // Then
        assertEquals(
            RectangleTargetStrategy(collisions, gameObject, 1),
            gameObject.interactTarget
        )
    }

    @ParameterizedTest
    @ValueSource(ints = [12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24])
    fun `Other object strategy`(type: Int) {
        // When
        val gameObject = factory.spawn(1, Tile.EMPTY, type, 0)
        // Then
        assertEquals(
            EntityTileTargetStrategy(gameObject),
            gameObject.interactTarget
        )
    }

}