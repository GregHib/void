package world.gregs.voidps.engine.client.ui

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_ID
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import world.gregs.voidps.engine.entity.character.player.Player

internal class InterfaceExtensionsTest : InterfaceTest() {

    private lateinit var player: Player

    val name = "interface_name"

    @BeforeEach
    override fun setup() {
        super.setup()
        player = mockk(relaxed = true)
        every { player.gameFrame } returns gameframe
        every { player.interfaces } returns manager
    }

    @Test
    fun `Open by name`() {
        every { definitions.get(name) } returns InterfaceDefinition(id = 0)
        assertFalse(player.open(name))
        verify { manager.open(name) }
        verify(exactly = 0) { manager.close(any<String>()) }
    }

    @Test
    fun `Interface already open with same type is closed first`() {
        val extras = mapOf(
            "type" to "interface_type",
            "parent_fixed" to ROOT_ID,
            "index_fixed" to ROOT_INDEX
        )
        every { definitions.get("first") } returns InterfaceDefinition(id = 0, extras = extras)
        every { definitions.get("second") } returns InterfaceDefinition(id = 1, extras = extras)
        manager.open("first")
        val result = player.open("second")
        verifyOrder {
            manager.close("first")
            manager.open("second")
        }
        assertTrue(result)
    }

    @Test
    fun `Interface name is open`() {
        val result = player.isOpen(name)
        verify { manager.contains(name) }
        assertFalse(result)
    }

    @Test
    fun `Has interface type open`() {
        val result = player.hasOpen("interface_type")
        verify { manager.get("interface_type") }
        assertFalse(result)
    }

    @Test
    fun `Close interface name`() {
        assertFalse(player.close(name))
        verify { manager.close(name) }
    }

    @Test
    fun `Close interface type`() {
        every { definitions.get("second") } returns InterfaceDefinition()
        every { manager.get("interface_type") } returns "second"
        val result = player.closeType("interface_type")
        verifyOrder {
            manager.get("interface_type")
            manager.close("second")
        }
        assertFalse(result)
    }

    @Test
    fun `Close children`() {
        assertFalse(player.closeChildren(name))
        verify { manager.closeChildren(name) }
    }

    @Test
    fun `Suspend interface`() = runBlocking {
        val action: Action = mockk()
        val interfaces: Interfaces = mockk()
        every { player.interfaces } returns interfaces
        every { player.action } returns action
        every { interfaces.get("main_screen") } returns "four"
        val suspension = Suspension.Interface("four")
        coEvery { action.await<Unit>(suspension) } returns Unit
        assertTrue(player.awaitInterfaces())
        coVerify { action.await<Unit>(suspension) }
    }
}
