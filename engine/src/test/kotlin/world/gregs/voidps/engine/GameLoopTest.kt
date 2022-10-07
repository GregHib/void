package world.gregs.voidps.engine

import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class GameLoopTest {

    private lateinit var loop: GameLoop

    @RelaxedMockK
    private lateinit var stage: Runnable

    @BeforeEach
    fun setup() {
        loop = GameLoop(listOf(stage), UnconfinedTestDispatcher())
    }

    @Test
    fun `Game loop`() {
        loop.tick(stage)
        // Then
        verify {
            stage.run()
        }
    }
}