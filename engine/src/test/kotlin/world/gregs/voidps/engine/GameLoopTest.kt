package world.gregs.voidps.engine

import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@ExtendWith(MockKExtension::class)
internal class GameLoopTest {

    private lateinit var loop: GameLoop
    @RelaxedMockK
    private lateinit var service: ScheduledExecutorService
    @RelaxedMockK
    private lateinit var stage: Runnable
    @RelaxedMockK
    private lateinit var stage2: Runnable

    @BeforeEach
    fun setup() {
        loop = GameLoop(service, listOf(stage, stage2))
    }

    @Test
    fun `Starting game loop`() {
        // When
        loop.start()
        // Then
        verify {
            service.scheduleAtFixedRate(loop, 0, 600L, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun `Game loop`() {
        loop.run()
        // Then
        verifyOrder {
            stage.run()
            stage2.run()
        }
    }

    @Test
    fun `Stop game loop`() {
        // When
        loop.stop()
        // Then
        verify {
            service.shutdown()
        }
    }
}