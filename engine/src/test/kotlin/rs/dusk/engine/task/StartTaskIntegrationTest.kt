package rs.dusk.engine.task

import io.mockk.spyk
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declare
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.event.then
import rs.dusk.engine.script.KoinMock
import rs.dusk.engine.tick.Tick
import rs.dusk.engine.tick.TickInput
import rs.dusk.engine.tick.TickUpdate

internal class StartTaskIntegrationTest : KoinMock() {

    lateinit var bus: EventBus

    override val modules = listOf(eventModule)

    @BeforeEach
    fun setup() {
        bus = declare { spyk(EventBus()) }
    }

    @Test
    fun `Start sub tasks are delayed until next call`() {
        // Given
        val executor = spyk(TaskExecutor())
        val startTask = StartTask()
        TickInput then {
            startTask.subTasks.add {
                bus.emit(TickUpdate)
            }
        }
        executor.execute(startTask)
        executor.repeat {
            bus.emit(TickInput)
            executor.delay {
                bus.emit(Tick(0))
            }
        }
        // When
        executor.run()
        executor.run()
        // Then
        verifyOrder {
            bus.emit(TickInput)
            bus.emit(Tick(0))
            bus.emit(TickUpdate)
        }
    }
}