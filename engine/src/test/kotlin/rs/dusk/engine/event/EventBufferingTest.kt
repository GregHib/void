package rs.dusk.engine.event

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declare
import org.koin.test.mock.declareMock
import rs.dusk.engine.model.engine.Tick
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerEvent
import rs.dusk.engine.model.entity.index.player.PlayerUnregistered
import rs.dusk.engine.script.ScriptMock
import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
internal class EventBufferingTest : ScriptMock() {

    lateinit var bus: EventBus
    lateinit var buffer: EventBuffer
    lateinit var player: Player

    lateinit var tickHandler: Tick.(Tick) -> Unit
    lateinit var unregisteredHandler: PlayerUnregistered.(PlayerUnregistered) -> Unit

    @BeforeEach
    override fun setup() {
        bus = declareMock {
            every { add<Unit, PlayerEvent>(any(), any()) } answers {
                val clazz: KClass<Event<Unit>> = arg(0)
                val handler: EventHandler<Unit, Event<Unit>> = arg(1)
                if(Tick::class == clazz) {
                    tickHandler = handler.action
                } else {
                    unregisteredHandler = handler.action
                }
            }
            every { emit(Tick) } answers {
                tickHandler.invoke(Tick, Tick)
            }
            every { emit(any<PlayerUnregistered>()) } answers {
                val event: PlayerUnregistered = arg(0)
                unregisteredHandler.invoke(event, event)
            }
            every { emit(any<PlayerEvent>()) } returns Unit
        }
        buffer = declare { spyk(EventBuffer(10)) }
        player = mockk()
        super.setup()
    }

    @Test
    fun `Process events on tick`() {
        // Given
        val event: PlayerEvent = mockk()
        every { event.player } returns player
        buffer.buffered[player] = mutableListOf(event)
        // When
        bus.emit(Tick)
        // Then
        verify {
            bus.emit(Tick)
            bus.emit(event)
        }
        assertFalse(buffer.buffered[player]!!.contains(event))
    }

    @Test
    fun `Remove buffer when unregistered`() {
        // When
        bus.emit(PlayerUnregistered(player))
        // Then
        verify {
            buffer.remove(player)
        }
    }
}