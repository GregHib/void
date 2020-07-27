package rs.dusk.engine.client.ui

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rs.dusk.engine.action.Action
import rs.dusk.engine.action.Suspension
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import rs.dusk.engine.model.entity.character.player.Player

internal class InterfaceExtensionsTest : InterfaceTest() {

    private lateinit var player: Player

    @BeforeEach
    override fun setup() {
        super.setup()
        player = mockk(relaxed = true)
        every { player.gameFrame } returns gameframe
        every { player.interfaces } returns manager
    }

    @Test
    fun `Open by name`() {
        every { lookup.get("interface_name") } returns Interface(id = 0, type = null)
        assertThrows<Interface.InvalidInterfaceException> {
            player.open("interface_name")
        }
        verify { manager.open("interface_name") }
        verify(exactly = 0) { manager.close(any<Int>()) }
    }

    @Test
    fun `Interface already open with same type is closed first`() {
        interfaces[0] = Interface(id = 0, type = "interface_type", data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(id = 1, type = "interface_type", data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["interface_name"] = 1
        manager.open(0)
        val result = player.open("interface_name")
        verifyOrder {
            manager.close(0)
            manager.open("interface_name")
        }
        assertTrue(result)
    }

    @Test
    fun `Interface name is open`() {
        val result = player.isOpen("interface_name")
        verify { manager.contains("interface_name") }
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
        assertThrows<InterfacesLookup.IllegalNameException> {
            player.close("interface_name")
        }
        verify { manager.close("interface_name") }
    }

    @Test
    fun `Close interface type`() {
        every { manager.get("interface_type") } returns 1
        val result = player.closeType("interface_type")
        verifyOrder {
            manager.get("interface_type")
            manager.close(1)
        }
        assertFalse(result)
    }

    @Test
    fun `Close children`() {
        assertThrows<InterfacesLookup.IllegalNameException> {
            player.closeChildren("interface_name")
        }
        verify { manager.closeChildren("interface_name") }
    }

    @Test
    fun `Suspend interface`() = runBlocking {
        val action: Action = mockk()
        val interfaces: Interfaces = mockk()
        every { player.interfaces } returns interfaces
        every { player.action } returns action
        every { interfaces.get("main_screen") } returns  4
        val suspension = Suspension.Interface(4)
        coEvery { action.await<Unit>(suspension) } returns Unit
        assertTrue(player.awaitInterfaces())
        coVerify { action.await<Unit>(suspension) }
    }
}
