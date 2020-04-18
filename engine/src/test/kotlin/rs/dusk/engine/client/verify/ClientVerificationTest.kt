package rs.dusk.engine.client.verify

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.script.KoinMock
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since April 09, 2020
 */
internal class ClientVerificationTest : KoinMock() {

    private class TestMessage : Message {
        companion object : MessageCompanion<TestMessage>()
    }

    @BeforeEach
    fun setup() {
        loadModules(clientVerificationModule)
    }

    @Test
    fun verify() {
        // Given
        val bus = declareMock<ClientVerification> {
            every { add<TestMessage>(any(), any()) } just Runs
        }
        val action: TestMessage.(Player) -> Unit = mockk(relaxed = true)
        // When
        TestMessage verify action
        // Then
        verify {
            bus.add<TestMessage>(any(), any())
        }
    }
}