package world.gregs.voidps.engine.client

import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import world.gregs.voidps.engine.getIntProperty
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.NetworkQueue

@OptIn(ExperimentalCoroutinesApi::class)
internal class ConnectionQueueTest : KoinMock() {

    private lateinit var queue: NetworkQueue

    override val modules = listOf(
        module {
            single {
                ConnectionQueue(getIntProperty("connectionPerTickCap", 1))
            }
        }
    )

    @BeforeEach
    fun setup() {
        queue = spyk(ConnectionQueue(25))
    }

    @Test
    fun `Await login`() = runTest(UnconfinedTestDispatcher()) {
        launch {
            queue.await()
        }
        queue.run()
    }
}