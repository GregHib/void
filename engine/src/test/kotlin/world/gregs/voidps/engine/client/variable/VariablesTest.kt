package world.gregs.voidps.engine.client.variable

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.sendVarbit
import world.gregs.voidps.engine.client.sendVarc
import world.gregs.voidps.engine.client.sendVarcStr
import world.gregs.voidps.engine.client.sendVarp
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.definition.config.VariableDefinition
import world.gregs.voidps.engine.event.Events

internal class VariablesTest {

    private lateinit var definitions: VariableDefinitions
    private lateinit var variables: Variables
    private lateinit var variable: VariableDefinition
    private lateinit var player: Player
    private lateinit var events: Events
    private lateinit var map: MutableMap<String, Any>

    @BeforeEach
    fun setup() {
        map = mutableMapOf()
        variable = mockk(relaxed = true)
        every { variable.persistent } returns true
        every { variable.defaultValue } returns 0
        every { variable.format } returns VariableFormat.INT
        definitions = mockk(relaxed = true)
        variables = spyk(Variables(map))
        events = mockk(relaxed = true)
        player = mockk(relaxed = true)
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        every { player.sendVarp(any(), any()) } just Runs
        every { player.sendVarbit(any(), any()) } just Runs
        every { player.sendVarc(any(), any()) } just Runs
        every { player.sendVarcStr(any(), any()) } just Runs
        every { player.variables } returns variables
        every { player.events } returns events
        every { definitions.get(key) } returns variable
        variables.link(player, definitions)
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
            events.emit(VariableSet(key, 1, 42))
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
        every { variable.type } returns VariableType.Varp
        // When
        variables.send(key)
        // Then
        verify { player.sendVarp(variable.id, 0) }
    }

    @Test
    fun `Send varbit`() {
        // Given
        every { variable.type } returns VariableType.Varbit
        // When
        variables.send(key)
        // Then
        verify { player.sendVarbit(variable.id, 0) }
    }

    @Test
    fun `Send varc`() {
        // Given
        every { variable.type } returns VariableType.Varc
        // When
        variables.send(key)
        // Then
        verify { player.sendVarc(variable.id, 0) }
    }

    @Test
    fun `Send varcstr`() {
        // Given
        val variable = mockk<VariableDefinition>(relaxed = true)
        every { variable.type } returns VariableType.Varcstr
        every { variable.defaultValue } returns "nothing"
        every { definitions.get(key) } returns variable
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
        every { definitions.get(key) } returns null
        // Given
//        definitions.clear()
        // When
        val result = variables.get(key, -1)
        // Then
        assertEquals(-1, result)
    }

    @Test
    fun `Add bitwise`() {
        // Given
        val variable = VariableDefinition(0, VariableType.Varp, VariableFormat.BITWISE, "First", true, listOf("First", "Second"))
        map[key] = 0
        every { definitions.get(key) } returns variable
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
        val variable = VariableDefinition(0, VariableType.Varp, VariableFormat.BITWISE, "First", true, listOf("First", "Second"))
        map[key] = 1
        every { definitions.get(key) } returns variable
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
        val variable = VariableDefinition(0, VariableType.Varp, VariableFormat.BITWISE, "First", true, listOf("First", "Second"))
        map[key] = 1
        every { definitions.get(key) } returns variable
        // When
        variables.add(key, "First", true)
        // Then
        assertEquals(1, map[key])//Doesn't change
        verify(exactly = 0) { variables.send(key) }
    }

    @Test
    fun `Add bitwise no refresh`() {
        // Given
        val variable = VariableDefinition(0, VariableType.Varp, VariableFormat.BITWISE, "First", true, listOf("First", "Second"))
        map[key] = 0
        every { definitions.get(key) } returns variable
        // When
        variables.add(key, "First", false)
        // Then
        assertEquals(1, map[key])
        verify(exactly = 0) { variables.send(key) }
    }

    @Test
    fun `Remove bitwise`() {
        // Given
        val variable = VariableDefinition(0, VariableType.Varp, VariableFormat.BITWISE, "First", true, listOf("First", "Second"))
        map[key] = 3
        every { definitions.get(key) } returns variable
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
        val variable = VariableDefinition(0, VariableType.Varp, VariableFormat.BITWISE, "First", true, listOf("First", "Second"))
        map[key] = 3
        every { definitions.get(key) } returns variable
        // When
        variables.remove(key, "First", false)
        // Then
        assertEquals(2, map[key])
        verify(exactly = 0) { variables.send(key) }
    }

    @Test
    fun `Persistence uses different variable map`() {
        // Given
        val variable = VariableDefinition(0, VariableType.Varp, VariableFormat.BITWISE, "First", false, listOf("First", "Second"))
        variables.temporaryVariables[key] = 3
        every { definitions.get(key) } returns variable
        // When
        variables.remove(key, "First", false)
        // Then
        assertEquals(2, variables.temporaryVariables[key])
        verify(exactly = 0) { variables.send(key) }
    }

    @Test
    fun `Clear bitwise of multiple values`() {
        // Given
        val variable = VariableDefinition(0, VariableType.Varp, VariableFormat.BITWISE, 0, true, listOf("First", "Second"))
        map[key] = 3
        every { definitions.get(key) } returns variable
        // When
        variables.clear(key, true)
        // Then
        assertNull(map[key])
        verifyOrder {
            variables.send(key)
            events.emit(VariableSet(key, 3, 0))
        }
    }

    companion object {
        private const val key = "key"
    }
}