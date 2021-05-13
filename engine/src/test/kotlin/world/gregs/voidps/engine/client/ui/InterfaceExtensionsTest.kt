package world.gregs.voidps.engine.client.ui

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_ID
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import world.gregs.voidps.engine.client.ui.detail.InterfaceData
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetail
import world.gregs.voidps.engine.entity.character.player.Player

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
        every { details.get("interface_name") } returns InterfaceDetail(id = 0, type = "")
        assertThrows<InterfaceDetail.InvalidInterfaceException> {
            player.open("interface_name")
        }
        verify { manager.open("interface_name") }
        verify(exactly = 0) { manager.close(any<String>()) }
    }

    @Test
    fun `Interface already open with same type is closed first`() {
        interfaces["first"] = InterfaceDetail(id = 0, name = "first", type = "interface_type", data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        interfaces["second"] = InterfaceDetail(id = 1, name = "second", type = "interface_type", data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names[0] = "first"
        names[1] = "second"
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
        assertFalse(player.close("interface_name"))
        verify { manager.close("interface_name") }
    }

    @Test
    fun `Close interface type`() {
        interfaces["second"] = mockk()
        names[1] = "second"
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
        assertFalse(player.closeChildren("interface_name"))
        verify { manager.closeChildren("interface_name") }
    }

    @Test
    fun `Suspend interface`() = runBlocking {
        val action: Action = mockk()
        val interfaces: Interfaces = mockk()
        names[4] = "four"
        every { player.interfaces } returns interfaces
        every { player.action } returns action
        every { interfaces.get("main_screen") } returns "four"
        val suspension = Suspension.Interface("four")
        coEvery { action.await<Unit>(suspension) } returns Unit
        assertTrue(player.awaitInterfaces())
        coVerify { action.await<Unit>(suspension) }
    }
}
