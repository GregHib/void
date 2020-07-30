package rs.dusk.engine

import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.task.DelayTask
import rs.dusk.engine.task.RepeatTask
import rs.dusk.engine.task.StartTask
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.tick.Tick
import rs.dusk.engine.tick.TickInput
import rs.dusk.engine.tick.TickUpdate
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@ExtendWith(MockKExtension::class)
internal class GameLoopTest {

    private lateinit var loop: GameLoop
    private lateinit var executor: TaskExecutor
    @RelaxedMockK
    private lateinit var bus: EventBus
    @RelaxedMockK
    private lateinit var service: ScheduledExecutorService

    @BeforeEach
    fun setup() {
        executor = spyk(TaskExecutor())
        loop = GameLoop(bus, executor, service)
    }

    @Test
    fun `Starting game loop`() {
        // When
        loop.start()
        // Then
        verify {
            service.scheduleAtFixedRate(executor, 0, 600L, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun `Setup game loop`() {
        // Given
        val start = StartTask()
        // When
        loop.setup(start)
        // Then
        verifyOrder {
            executor.execute(any<DelayTask>())
            executor.execute(start)
            executor.execute(any<RepeatTask>())
        }
    }

    @Test
    fun `Game loop`() {
        loop.setup(mockk(relaxed = true))
        loop.start()
        // When
        executor.run()
        // Then
        verify {
            bus.emit(TickInput)
            bus.emit(Tick(0))
            bus.emit(TickUpdate)
        }
    }

    @Test
    fun `Stop game loop`() {
        // When
        loop.stop()
        // Then
        verify {
            executor.clear()
            service.shutdown()
        }
    }
}