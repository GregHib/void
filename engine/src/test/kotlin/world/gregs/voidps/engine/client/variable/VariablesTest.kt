package world.gregs.voidps.engine.client.variable

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.encode.sendVarbit
import world.gregs.voidps.network.encode.sendVarc
import world.gregs.voidps.network.encode.sendVarcStr
import world.gregs.voidps.network.encode.sendVarp

internal class VariablesTest {

    private lateinit var store: VariableStore
    private lateinit var variables: Variables
    private lateinit var variable: Variable<Int>
    private lateinit var player: Player
    private lateinit var events: Events
    private lateinit var map: MutableMap<String, Any>

    @BeforeEach
    fun setup() {
        map = mutableMapOf()
        variable = mockk(relaxed = true)
        every { variable.persistent } returns true
        store = mockk(relaxed = true)
        variables = spyk(Variables(map))
        events = mockk(relaxed = true)
        player = mockk(relaxed = true)
        mockkStatic("world.gregs.voidps.network.encode.VarpEncoderKt")
        every { player.sendVarp(any(), any()) } just Runs
        mockkStatic("world.gregs.voidps.network.encode.VarbitEncoderKt")
        every { player.sendVarbit(any(), any()) } just Runs
        mockkStatic("world.gregs.voidps.network.encode.VarcEncoderKt")
        every { player.sendVarc(any(), any()) } just Runs
        mockkStatic("world.gregs.voidps.network.encode.VarcStrEncoderKt")
        every { player.sendVarcStr(any(), any()) } just Runs
        every { player.variables } returns variables
        every { player.events } returns events
        every { store.get(key) } returns variable
        variables.link(player, store)
    }

    @Test
    fun `Set value`() {
        // Given
        map[key] = 1
        every { variables.send(any()) } just Runs
        // When
        variables.set(key, 42, true)
        // Then
        assertEquals(42, map[key])
        verify {
            variables.send(any())
            events.emit(VariableSet(key, 42))
        }
    }

    @Test
    fun `Set removes default value`() {
        // Given
        every { variable.defaultValue } returns 42
        map[key] = 1
        every { variables.send(any()) } just Runs
        // When
        variables.set(key, 42, true)
        // Then
        assertTrue(player.variables.variables.isEmpty())
        verify { variables.send(any()) }
    }

    @Test
    fun `Set value no refresh`() {
        // Given
        map[key] = 1
        every { variables.send(any()) } just Runs
        // When
        variables.set(key, 42, false)
        // Then
        assertEquals(42, map[key])
        verify(exactly = 0) { variables.send(any()) }
    }

    @Test
    fun `Send varp`() {
        // Given
        every { variable.type } returns Variable.Type.VARP
        // When
        variables.send(key)
        // Then
        verify { player.sendVarp(variable.id, 0) }
    }

    @Test
    fun `Send varbit`() {
        // Given
        every { variable.type } returns Variable.Type.VARBIT
        // When
        variables.send(key)
        // Then
        verify { player.sendVarbit(variable.id, 0) }
    }

    @Test
    fun `Send varc`() {
        // Given
        every { variable.type } returns Variable.Type.VARC
        // When
        variables.send(key)
        // Then
        verify { player.sendVarc(variable.id, 0) }
    }

    @Test
    fun `Send varcstr`() {
        // Given
        val variable = mockk<Variable<String>>(relaxed = true)
        every { variable.type } returns Variable.Type.VARCSTR
        every { variable.defaultValue } returns "nothing"
        every { store.get(key) } returns variable
        // When
        variables.send(key)
        // Then
        verify { player.sendVarcStr(variable.id, "nothing") }
    }

    @Test
    fun `Get variable`() {
        // Given
        map[key] = 42
        // When
        val result = variables.get(key, -1)
        // Then
        assertEquals(42, result)
    }

    @Test
    fun `Get default value`() {
        // Given
        every { variable.defaultValue } returns 42
        // When
        val result = variables.get(key, -1)
        // Then
        assertEquals(42, result)
    }

    @Test
    fun `Get no variable`() {
        every { store.get(key) } returns null
        // Given
        store.clear()
        // When
        val result = variables.get(key, -1)
        // Then
        assertEquals(-1, result)
    }

    @Test
    fun `Add bitwise`() {
        // Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        map[key] = 0
        every { store.get(key) } returns variable
        // When
        variables.add(key, "First", true)
        // Then
        assertEquals(1, map[key])
        verify {
            variables.send(key)
            events.emit(VariableAdded(key, "First", 0, 1))
        }
    }

    @Test
    fun `Add bitwise two`() {
        // Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        map[key] = 1
        every { store.get(key) } returns variable
        // When
        variables.add(key, "Second", true)
        // Then
        assertEquals(3, map[key])
        verify {
            variables.send(key)
            events.emit(VariableAdded(key, "Second", 1, 3))
        }
    }

    @Test
    fun `Add bitwise existing`() {
        // Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        map[key] = 1
        every { store.get(key) } returns variable
        // When
        variables.add(key, "First", true)
        // Then
        assertEquals(1, map[key])//Doesn't change
        verify(exactly = 0) {variables.send(key) }
    }

    @Test
    fun `Add bitwise no refresh`() {
        // Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        map[key] = 0
        every { store.get(key) } returns variable
        // When
        variables.add(key, "First", false)
        // Then
        assertEquals(1, map[key])
        verify(exactly = 0) { variables.send(key) }
    }

    @Test
    fun `Remove bitwise`() {
        // Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        map[key] = 3
        every { store.get(key) } returns variable
        // When
        variables.remove(key, "First", true)
        // Then
        assertEquals(2, map[key])
        verify {
            variables.send(key)
            events.emit(VariableRemoved(key, "First", 3, 2))
        }
    }

    @Test
    fun `Remove bitwise no refresh`() {
        // Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        map[key] = 3
        every { store.get(key) } returns variable
        // When
        variables.remove(key, "First", false)
        // Then
        assertEquals(2, map[key])
        verify(exactly = 0) { variables.send(key) }
    }

    @Test
    fun `Persistence uses different variable map`() {
        // Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = false)
        variables.temporaryVariables[key] = 3
        every { store.get(key) } returns variable
        // When
        variables.remove(key, "First", false)
        // Then
        assertEquals(2, variables.temporaryVariables[key])
        verify(exactly = 0) { variables.send(key) }
    }

    @Test
    fun `Clear bitwise of multiple values`() {
        // Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        map[key] = 3
        every { store.get(key) } returns variable
        // When
        variables.clear<String>(key, true)
        // Then
        assertNull(map[key])
        verifyOrder {
            variables.send(key)
            events.emit(VariableRemoved(key, "First", 3, 2))
            variables.send(key)
            events.emit(VariableRemoved(key, "Second", 2, 0))
        }
    }

    companion object {
        private const val key = "key"
    }
}