package rs.dusk.engine.client.verify

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.mock.declareMock
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.script.KoinMock
import rs.dusk.engine.script.koin.KoinTestExtension
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 09, 2020
 */
internal class ClientVerificationTest : KoinMock() {

    private class TestMessage : Message {
        companion object : MessageCompanion<TestMessage>()
    }

    val verifier by inject<ClientVerification>()

    override val modules = listOf(clientVerificationModule)

    @Test
    fun `Add verification`() {
        // Given
        val verifier = mockk<Verification<TestMessage>>(relaxed = true)
        val clazz = TestMessage::class
        // When
        this.verifier.add(clazz, verification = verifier)
        // Then
        assertEquals(verifier, this.verifier.get(clazz))
    }

    @Test
    fun `Add duplicate verification`() {
        // Given
        val clazz = TestMessage::class
        val verifier = mockk<Verification<TestMessage>>(relaxed = true)
        this.verifier.add(clazz, verification = mockk(relaxed = true))
        // Then
        assertThrows<IllegalArgumentException> {
            this.verifier.add(clazz, verification = verifier)
        }
    }

    @Test
    fun `Get empty verification`() {
        // Given
        val clazz = TestMessage::class
        // When
        val result = verifier.get(clazz)
        // Then
        assertNull(result)
    }

    @Test
    fun `Verify message`() {
        // Given
        val clazz = TestMessage::class
        val verification = mockk<Verification<TestMessage>>(relaxed = true)
        verifier.add(clazz, verification = verification)
        val message = TestMessage()
        val player: Player = mockk(relaxed = true)
        // When
        verifier.verify(player, message)
        // Then
        verify {
            verification.block(message, player)
        }
    }

    @Test
    fun `Verify message no verification`() {
        // Given
        val message = TestMessage()
        val player: Player = mockk(relaxed = true)
        // Then
        assertThrows<IllegalArgumentException> {
            verifier.verify(player, message)
        }
    }

    @Test
    fun `Verify using extension`() {
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