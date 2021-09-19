package world.gregs.voidps.engine.client.ui

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.menu.InterfaceOptionSettings.getHash
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.network.encode.sendInterfaceSettings
import world.gregs.voidps.network.encode.sendScript

internal class InterfaceOptionsTest {

    lateinit var options: InterfaceOptions

    lateinit var player: Player

    lateinit var definitions: InterfaceDefinitions

    lateinit var containerDefinitions: ContainerDefinitions

    private val staticOptions = arrayOf("", "", "", "", "", "", "", "", "", "Examine")
    private val overrideOptions = arrayOf("Option1")
    private val name = "name"
    private val comp = "component"

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        definitions = mockk(relaxed = true)
        containerDefinitions = mockk(relaxed = true)
        options = InterfaceOptions(player, definitions, containerDefinitions)
        every { definitions.get(name) } returns InterfaceDefinition(
            extras = mapOf("componentNames" to mapOf(comp to 0)),
            components = mapOf(
                0 to InterfaceComponentDefinition(
                    id = 0,
                    extras = mapOf(
                    "parent" to 5,
                    "container" to "container",
                    "primary" to false,
                    "options" to staticOptions
                ))
            ))
        mockkStatic("world.gregs.voidps.network.encode.InterfaceEncodersKt")
        every { player.sendInterfaceSettings(any(), any(), any(), any(), any()) } just Runs
        mockkStatic("world.gregs.voidps.network.encode.ScriptEncoderKt")
        every { player.sendScript(any(), *anyVararg()) } just Runs
    }

    @Test
    fun `Get returns static options by default`() {
        assertArrayEquals(emptyArray(), options.get(name, "unknown"))
        assertEquals("", options.get(name, "unknown", 9))
        assertArrayEquals(staticOptions, options.get(name, comp))
        assertEquals("", options.get(name, comp, 0))
        assertEquals("Examine", options.get(name, comp, 9))
    }

    @Test
    fun `Options can be overridden`() {
        assertTrue(options.set(name, comp, overrideOptions))
        assertArrayEquals(overrideOptions, options.get(name, comp))
    }

    @Test
    fun `Removing options returns to use static`() {
        options.set(name, comp, overrideOptions)

        assertTrue(options.remove(name, comp))
        assertFalse(options.remove(name, comp))
        assertArrayEquals(staticOptions, options.get(name, comp))
    }

    @Test
    fun `Set individual options`() {
        assertTrue(options.set(name, comp, 0, "option"))
        assertArrayEquals(arrayOf("option", "", "", "", "", "", "", "", "", "Examine"), options.get(name, comp))
    }

    @Test
    fun `Send all options`() {
        every { containerDefinitions.get(any<String>()) } returns ContainerDefinition(10, extras = mapOf("width" to 2, "height" to 3))
        options.send(name, comp)
        verify {
            player.sendScript(695, (5 shl 16) or 0, 10, 2, 3, 0, -1, "", "", "", "", "", "", "", "", "")
        }
    }

    @Test
    fun `Unlock all options`() {
        every { containerDefinitions.get(name) } returns ContainerDefinition(10, extras = mapOf("width" to 2, "height" to 3))
        options.unlockAll(name, comp, 0..27)
        verify {
            player.sendInterfaceSettings(5, 0, 0, 27, getHash(9))
        }
    }

    @Test
    fun `Unlock few options`() {
        every { containerDefinitions.get(name) } returns ContainerDefinition(10, extras = mapOf("width" to 2, "height" to 3))
        options.set(name, comp, arrayOf("one", "two", "three"))
        options.unlock(name, comp, 0..27, "two", "three")
        verify {
            player.sendInterfaceSettings(5, 0, 0, 27, getHash(1, 2))
        }
    }

    @Test
    fun `Lock all options`() {
        every { containerDefinitions.get(name) } returns ContainerDefinition(10, extras = mapOf("width" to 2, "height" to 3))
        options.lockAll(name, comp, 0..27)
        verify {
            player.sendInterfaceSettings(5, 0, 0, 27, 0)
        }
    }
}