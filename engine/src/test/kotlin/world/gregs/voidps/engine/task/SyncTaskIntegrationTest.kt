package world.gregs.voidps.engine.task

import io.mockk.spyk
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declare
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.tick.Tick
import world.gregs.voidps.engine.tick.TickInput
import world.gregs.voidps.engine.tick.TickUpdate

internal class SyncTaskIntegrationTest : KoinMock() {

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
        val startTask = SyncTask()
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