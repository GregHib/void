package world.gregs.voidps.engine.client.ui

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.client.sendInterfaceSettings
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.menu.InterfaceOptionSettings.getHash
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.entity.character.player.Player

internal class InterfaceOptionsTest {

    private lateinit var options: InterfaceOptions
    private lateinit var player: Player
    private lateinit var definitions: InterfaceDefinitions
    private lateinit var inventoryDefinitions: InventoryDefinitions

    private val staticOptions = arrayOf("", "", "", "", "", "", "", "", "", "Examine")
    private val name = "name"
    private val comp = "component"

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        definitions = mockk(relaxed = true)
        inventoryDefinitions = mockk(relaxed = true)
        options = InterfaceOptions(player, definitions, inventoryDefinitions)
        every { definitions.getComponent(any<String>(), any<String>()) } returns null
        every { definitions.getComponent(name, comp) } returns InterfaceComponentDefinition(
            id = 0,
            extras = mapOf(
                "parent" to 5,
                "inventory" to "inventory",
                "primary" to false,
                "options" to staticOptions
            ))
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        every { player.sendInterfaceSettings(any(), any(), any(), any(), any()) } just Runs
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
    fun `Send all options`() {
        every { inventoryDefinitions.get(any<String>()) } returns InventoryDefinition(10, extras = mapOf("width" to 2, "height" to 3))
        options.send(name, comp)
        verify {
            player.sendScript(695, (5 shl 16) or 0, 10, 2, 3, 0, -1, "", "", "", "", "", "", "", "", "")
        }
    }

    @Test
    fun `Unlock all options`() {
        every { inventoryDefinitions.get(name) } returns InventoryDefinition(10, extras = mapOf("width" to 2, "height" to 3))
        options.unlockAll(name, comp, 0..27)
        verify {
            player.sendInterfaceSettings(5, 0, 0, 27, getHash(9))
        }
    }

    @Test
    fun `Unlock few options`() {
        every { definitions.getComponent(name, comp) } returns InterfaceComponentDefinition(
            id = 0,
            extras = mapOf(
                "parent" to 5,
                "inventory" to "inventory",
                "primary" to false,
                "options" to arrayOf("one", "two", "three")
            )
        )
        every { inventoryDefinitions.get(name) } returns InventoryDefinition(10, extras = mapOf("width" to 2, "height" to 3))
        options.unlock(name, comp, 0..27, "two", "three")
        verify {
            player.sendInterfaceSettings(5, 0, 0, 27, getHash(1, 2))
        }
    }

    @Test
    fun `Lock all options`() {
        every { inventoryDefinitions.get(name) } returns InventoryDefinition(10, stringId = "10", extras = mapOf("width" to 2, "height" to 3))
        options.lockAll(name, comp, 0..27)
        verify {
            player.sendInterfaceSettings(5, 0, 0, 27, 0)
        }
    }
}