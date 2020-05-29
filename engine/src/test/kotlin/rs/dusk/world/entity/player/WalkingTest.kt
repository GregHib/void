package rs.dusk.world.entity.player

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.get
import org.koin.test.mock.declareMock
import rs.dusk.engine.client.verify.ClientVerification
import rs.dusk.engine.client.verify.clientVerificationModule
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.path.PathFinder
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.pathFindModule
import rs.dusk.engine.script.ScriptMock
import rs.dusk.network.rs.codec.game.decode.message.WalkMapMessage
import rs.dusk.network.rs.codec.game.decode.message.WalkMiniMapMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
internal class WalkingTest : ScriptMock() {

    lateinit var pf: PathFinder

    @BeforeEach
    override fun setup() {
        loadModules(pathFindModule, clientVerificationModule)
        pf = declareMock()
        super.setup()
    }

    @Test
    fun `Map click moves`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val verifier: ClientVerification = get()
        val message = WalkMapMessage(1, 2, true)
        every { pf.find(any(), any<Tile>()) } returns PathResult.Failure
        // When
        verifier.verify(player, message)
        // Then
        verify {
            player.movement.clear()
            pf.find(player, any<Tile>())
        }
    }

    @Test
    fun `Minimap click moves`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val verifier: ClientVerification = get()
        val message = WalkMiniMapMessage(1, 2, true)
        every { pf.find(any(), any<Tile>()) } returns PathResult.Failure
        // When
        verifier.verify(player, message)
        // Then
        verify {
            player.movement.clear()
            pf.find(player, any<Tile>())
        }
    }

}