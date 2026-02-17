package world.gregs.voidps.engine.client.ui

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.sendInterfaceSettings
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.menu.InterfaceOptionSettings.getHash
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.entity.character.player.Player

internal class InterfaceOptionsTest {

    private lateinit var options: InterfaceOptions
    private lateinit var player: Player

    private val staticOptions = arrayOf("", "", "", "", "", "", "", "", "", "Examine")
    private val name = "name"
    private val comp = "component"

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        options = InterfaceOptions(player)
        InventoryDefinitions.clear()
        InterfaceDefinitions.set(
            arrayOf(
                InterfaceDefinition(
                    components = mutableMapOf(
                        0 to InterfaceComponentDefinition(
                            id = InterfaceDefinition.pack(5, 0),
                            extras = mapOf(
                                "inventory" to "inventory",
                                "primary" to false,
                                "options" to staticOptions,
                            ),
                        )
                    )
                ),
                InterfaceDefinition(
                    components = mutableMapOf(
                        0 to InterfaceComponentDefinition(
                            id = InterfaceDefinition.pack(5, 0),
                            extras = mapOf(
                                "inventory" to "inventory",
                                "primary" to false,
                                "options" to arrayOf("one", "two", "three"),
                            ),
                        )
                    )
                )
            ), mapOf(name to 0, "${name}_2" to 1), mapOf("$name:$comp" to 0, "${name}_2:$comp" to 0)
        )
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        every { player.sendInterfaceSettings(any(), any(), any(), any()) } just Runs
        every { player.sendScript(any(), *anyVararg()) } just Runs
    }

    @Test
    fun `Send all options`() {
        InventoryDefinitions.set(arrayOf(InventoryDefinition(10, extras = mapOf("width" to 2, "height" to 3))), mapOf("inventory" to 0))
        options.send(name, comp)
        verify {
            player.sendScript("secondary_options", (5 shl 16) or 0, 10, 2, 3, 0, -1, "", "", "", "", "", "", "", "", "")
        }
    }

    @Test
    fun `Unlock all options`() {
        InventoryDefinitions.set(arrayOf(InventoryDefinition(10, extras = mapOf("width" to 2, "height" to 3))), mapOf(name to 0))
        options.unlockAll(name, comp, 0..27)
        verify {
            player.sendInterfaceSettings(327680, 0, 27, getHash(9))
        }
    }

    @Test
    fun `Unlock few options`() {
        InventoryDefinitions.set(arrayOf(InventoryDefinition(10, extras = mapOf("width" to 2, "height" to 3))), mapOf("${name}_2" to 0))
        options.unlock("${name}_2", comp, 0..27, "two", "three")
        verify {
            player.sendInterfaceSettings(327680, 0, 27, getHash(1, 2))
        }
    }

    @Test
    fun `Lock all options`() {
        InventoryDefinitions.set(arrayOf(InventoryDefinition(10, extras = mapOf("width" to 2, "height" to 3))), mapOf(name to 0))
        options.lockAll(name, comp, 0..27)
        verify {
            player.sendInterfaceSettings(327680, 0, 27, 0)
        }
    }
}
