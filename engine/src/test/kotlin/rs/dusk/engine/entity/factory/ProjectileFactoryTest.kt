package rs.dusk.engine.entity.factory

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.mock.declareMock
import rs.dusk.engine.entity.event.Registered
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventBusModule
import rs.dusk.engine.script.KoinMock
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
internal class ProjectileFactoryTest : KoinMock() {

    override val modules = listOf(module { single { ProjectileFactory() } }, eventBusModule)

    @Test
    fun `Spawn registers`() {
        // Given
        val factory: ProjectileFactory = get()
        val bus: EventBus = declareMock {
            every { emit(any<Registered>()) } just Runs
        }
        // When
        val projectile = factory.spawn(2)
        // Then
        assertEquals(2, projectile.id)
        verify { bus.emit<Registered>(any()) }
    }

}