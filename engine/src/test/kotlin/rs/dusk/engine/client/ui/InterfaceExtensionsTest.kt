package rs.dusk.engine.client.ui

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        assertThrows<InterfacesLookup.IllegalNameException> {
            player.open("interface_name")
        }
        verify { manager.open("interface_name") }
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
}
