package rs.dusk.engine.entity.character.player

import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.network.rs.codec.game.encode.ContextMenuOptionMessageEncoder

internal class PlayerOptionsTest {

    lateinit var player: Player
    lateinit var options: PlayerOptions
    lateinit var encoder: ContextMenuOptionMessageEncoder

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        encoder = mockk(relaxed = true)
        options = PlayerOptions(player, encoder)
        mockkStatic("rs.dusk.engine.client.SessionsKt")
    }

    @Test
    fun `Set option`() {
        assertTrue(options.set(1, "Attack"))
        verify {
            encoder.encode(player, "Attack", 1, false, -1)
        }
    }

    @Test
    fun `Index zero can't be set`() {
        assertFalse(options.set(0, "Attack"))
    }

    @Test
    fun `Index zero is walk here`() {
        assertTrue(options.has(0))
        assertEquals("Walk here", options.get(0))
    }

    @Test
    fun `Set option to top`() {
        assertTrue(options.set(1, "Attack", true))
    }

    @Test
    fun `Can't set invalid slot`() {
        assertFalse(options.set(0, "Attack"))
        assertFalse(options.set(8, "Attack"))
    }

    @Test
    fun `Can't override option`() {
        assertTrue(options.set(1, "Attack"))
        assertFalse(options.set(1, "Talk-to"))
    }

    @Test
    fun `Get option`() {
        options.set(2, "Follow")
        assertEquals("null", options.get(1))
        assertEquals("Follow", options.get(2))
    }

    @Test
    fun `Has option`() {
        options.set(2, "Follow")
        assertTrue(options.has(2))
        assertFalse(options.has(1))
    }

    @Test
    fun `Remove option`() {
        options.set(2, "Follow")
        assertTrue(options.has(2))
        options.remove(2)
        assertFalse(options.has(2))
        verify {
            encoder.encode(player, "null", 2, false, -1)
        }
    }

    @Test
    fun `Remove option by name`() {
        options.set(2, "Follow")
        assertTrue(options.has(2))
        assertTrue(options.remove("Follow"))
        assertFalse(options.has(2))
    }

    @Test
    fun `Can't remove non-existent name`() {
        assertFalse(options.remove("Follow"))
    }
}