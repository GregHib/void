package rs.dusk.engine.client.ui

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.detail.InterfaceComponentDetail
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.client.ui.menu.InterfaceOptionSettings.getHash
import rs.dusk.engine.entity.character.contain.detail.ContainerDetail
import rs.dusk.engine.entity.character.contain.detail.ContainerDetails
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.encode.message.InterfaceSettingsMessage
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage

internal class InterfaceOptionsTest {

    lateinit var options: InterfaceOptions

    lateinit var player: Player

    lateinit var details: InterfaceDetails

    lateinit var containerDetails: ContainerDetails

    private val staticOptions = arrayOf("", "", "", "", "", "", "", "", "", "Examine")
    private val overrideOptions = arrayOf("Option1")
    private val name = "name"
    private val comp = "component"

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        details = mockk(relaxed = true)
        containerDetails = mockk(relaxed = true)
        options = InterfaceOptions(player, details, containerDetails)
        val component = InterfaceComponentDetail(0, comp, parent = 5, container = "container", primaryContainer = false, options = staticOptions)
        every { details.getComponent(name, any()) } returns InterfaceComponentDetail(-1, "")
        every { details.getComponent(name, comp) } returns component
        mockkStatic("rs.dusk.engine.client.SessionsKt")
        every { player.send(any<Message>()) } just Runs
    }

    @Test
    fun `Get returns static options by default`() {
        assertEquals(emptyArray(), options.get(name, "unknown"))
        assertEquals("", options.get(name, "unknown", 9))
        assertEquals(staticOptions, options.get(name, comp))
        assertEquals("", options.get(name, comp, 0))
        assertEquals("Examine", options.get(name, comp, 9))
    }

    @Test
    fun `Options can be overridden`() {
        assertTrue(options.set(name, comp, overrideOptions))
        assertEquals(overrideOptions, options.get(name, comp))
    }

    @Test
    fun `Removing options returns to use static`() {
        options.set(name, comp, overrideOptions)

        assertTrue(options.remove(name, comp))
        assertFalse(options.remove(name, comp))
        assertEquals(staticOptions, options.get(name, comp))
    }

    @Test
    fun `Set individual options`() {
        assertTrue(options.set(name, comp, 0, "option"))
        assertEquals(arrayOf("option", "", "", "", "", "", "", "", "", "Examine"), options.get(name, comp))
    }

    @Test
    fun `Send all options`() {
        every { containerDetails.get(any<String>()) } returns ContainerDetail(10, 2, 3)
        options.send(name, comp)
        verify {
            player.send(ScriptMessage(695, (5 shl 16) or 0, 10, 2, 3, 0, -1, "", "", "", "", "", "", "", "", "", "Examine"))
        }
    }

    @Test
    fun `Unlock all options`() {
        every { containerDetails.get(any<Int>()) } returns ContainerDetail(10, 2, 3)
        options.unlockAll(name, comp, 0..27)
        verify {
            player.send(InterfaceSettingsMessage(5, 0, 0, 27, getHash(9)))
        }
    }

    @Test
    fun `Unlock few options`() {
        every { containerDetails.get(any<Int>()) } returns ContainerDetail(10, 2, 3)
        options.set(name, comp, arrayOf("one", "two", "three"))
        options.unlock(name, comp, 0..27, "two", "three")
        verify {
            player.send(InterfaceSettingsMessage(5, 0, 0, 27, getHash(1, 2)))
        }
    }

    @Test
    fun `Lock all options`() {
        every { containerDetails.get(any<Int>()) } returns ContainerDetail(10, 2, 3)
        options.lockAll(name, comp, 0..27)
        verify {
            player.send(InterfaceSettingsMessage(5, 0, 0, 27, 0))
        }
    }

    private fun assertEquals(expected: Array<String>, actual: Array<String>) {
        check(expected.contentEquals(actual)) { "expected: ${expected.toList()} but was: ${actual.toList()}" }
    }
}