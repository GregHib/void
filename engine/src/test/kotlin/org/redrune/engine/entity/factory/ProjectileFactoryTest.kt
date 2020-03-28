package org.redrune.engine.entity.factory

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.mock.declareMock
import org.redrune.engine.entity.event.Registered
import org.redrune.engine.event.EventBus
import org.redrune.engine.event.eventBusModule
import org.redrune.engine.script.KoinMock
import org.redrune.utility.get

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since March 30, 2020
 */
internal class ProjectileFactoryTest : KoinMock() {

    @BeforeEach
    fun setup() {
        loadModules(module { single { ProjectileFactory() } }, eventBusModule)
    }

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