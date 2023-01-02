package world.gregs.voidps.engine.entity.obj

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.utility.get

internal class GameObjectFactoryTest : KoinMock() {

    lateinit var factory: GameObjectFactory

    lateinit var collisions: Collisions

    override val modules = listOf(cacheDefinitionModule)

    @BeforeEach
    fun setup() {
        collisions = mockk(relaxed = true)
        val store: EventHandlerStore = mockk(relaxed = true)
        declareMock<ObjectDefinitions> {
            every { this@declareMock.get(any<String>()) } returns ObjectDefinition(id = 1, blockFlag = 1)
        }
        factory = GameObjectFactory(store, get())
    }

}