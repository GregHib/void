package org.redrune.engine.client.verify

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import org.redrune.core.network.model.message.Message
import org.redrune.engine.entity.model.Player
import org.redrune.engine.script.KoinMock

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