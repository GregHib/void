package world.gregs.voidps.engine.client.ui

import io.mockk.every
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_ID
import world.gregs.voidps.engine.entity.InterfaceApi
import world.gregs.voidps.network.login.protocol.encode.closeInterface
import world.gregs.voidps.network.login.protocol.encode.openInterface
import world.gregs.voidps.network.login.protocol.encode.updateInterface

internal class InterfacesMultipleTest : InterfaceTest() {

    private val zeroId = "zero"
    private val oneId = "one"
    private val twoId = "two"

    @BeforeEach
    override fun setup() {
        super.setup()
        every { definitions.get(any<Int>()) } returns InterfaceDefinition(-1)
        val zero = InterfaceDefinition(id = 0, stringId = "zero", type = "0", permanent = false, fixed = InterfaceDefinition.pack(1, 0))
        val one = InterfaceDefinition(id = 1, stringId = "one", type = "1", permanent = false, fixed = InterfaceDefinition.pack(2, 0))
        val two = InterfaceDefinition(id = 2, stringId = "two", type = "2", permanent = false)
        every { definitions.getOrNull(zeroId) } returns zero
        every { definitions.getOrNull(oneId) } returns one
        every { definitions.getOrNull(twoId) } returns two
        every { definitions.get(1) } returns one
        every { definitions.get(2) } returns two
    }

    @Test
    fun `Can't open child if parent isn't open`() {
        assertFalse(interfaces.open(oneId))

        verify(exactly = 0) {
            client.openInterface(any(), any(), any())
            InterfaceApi.open(any(), oneId)
        }
    }

    @Test
    fun `Can open parents and children`() {
        assertTrue(interfaces.open(twoId))
        assertTrue(interfaces.open(oneId))
        assertTrue(interfaces.open(zeroId))

        verifyOrder {
            client.updateInterface(2, 0)
            InterfaceApi.open(any(), twoId)
            client.openInterface(false, InterfaceDefinition.pack(2, 0), 1)
            InterfaceApi.open(any(), oneId)
            client.openInterface(false, InterfaceDefinition.pack(1, 0), 0)
            InterfaceApi.open(any(), zeroId)
        }
    }

    @Test
    fun `Remove doesn't close children`() {
        every { definitions.get(ROOT_ID) } returns InterfaceDefinition(id = 0)
        interfaces.open(twoId)
        interfaces.open(oneId)
        interfaces.open(zeroId)

        assertTrue(interfaces.remove(twoId))

        assertFalse(interfaces.contains(twoId))
        assertTrue(interfaces.contains(oneId))
        assertTrue(interfaces.contains(zeroId))

        verifyOrder {
            InterfaceApi.close(player, twoId)
        }
        verify(exactly = 0) {
            client.closeInterface(InterfaceDefinition.pack(2, 0))
            InterfaceApi.close(player, oneId)
            client.closeInterface(InterfaceDefinition.pack(1, 0))
            InterfaceApi.close(player, zeroId)
        }
    }

    @Test
    fun `Close children doesn't close parent`() {
        interfaces.open(twoId)
        interfaces.open(oneId)
        interfaces.open(zeroId)

        assertTrue(interfaces.closeChildren(twoId))

        assertTrue(interfaces.contains(twoId))
        assertFalse(interfaces.contains(oneId))
        assertFalse(interfaces.contains(zeroId))
        verifyOrder {
            client.closeInterface(InterfaceDefinition.pack(2, 0))
            InterfaceApi.close(player, oneId)
            client.closeInterface(InterfaceDefinition.pack(1, 0))
            InterfaceApi.close(player, zeroId)
        }
        verify(exactly = 0) {
            client.closeInterface(InterfaceDefinition.pack(0, 0))
            InterfaceApi.close(player, twoId)
        }
    }

    @Test
    fun `Close closes children and parent`() {
        every { definitions.get(ROOT_ID) } returns InterfaceDefinition(id = 0)
        interfaces.open(twoId)
        interfaces.open(oneId)
        interfaces.open(zeroId)

        assertTrue(interfaces.close(twoId))

        assertFalse(interfaces.contains(twoId))
        assertFalse(interfaces.contains(oneId))
        assertFalse(interfaces.contains(zeroId))
        verifyOrder {
            InterfaceApi.close(player, twoId)
            client.closeInterface(InterfaceDefinition.pack(2, 0))
            InterfaceApi.close(player, oneId)
            client.closeInterface(InterfaceDefinition.pack(1, 0))
            InterfaceApi.close(player, zeroId)
        }
    }
}
