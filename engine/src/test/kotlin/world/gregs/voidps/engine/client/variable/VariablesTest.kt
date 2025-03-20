package world.gregs.voidps.engine.client.variable

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.network.client.Client

internal class VariablesTest {

    private lateinit var definitions: VariableDefinitions
    private lateinit var variables: PlayerVariables
    private lateinit var variable: VariableDefinition
    private lateinit var player: Player
    private lateinit var client: Client
    private lateinit var events: EventDispatcher
    private lateinit var map: MutableMap<String, Any>

    private val id = 0
    private val default = "First"
    private val persist = true
    private val values = VariableValues(listOf("First", "Second"), "bitwise", default)
    private val transmit = true

    @BeforeEach
    fun setup() {
        map = mutableMapOf()
        variable = mockk(relaxed = true)
        every { variable.transmit } returns true
        every { variable.persistent } returns true
        every { variable.defaultValue } returns 0
        definitions = mockk(relaxed = true)
        events = mockk(relaxed = true)
        variables = spyk(PlayerVariables(events, map))
        variables.bits = VariableBits(variables, events)
        player = mockk(relaxed = true)
        client = mockk(relaxed = true)
        every { player.variables } returns variables
        every { definitions.get(key) } returns variable
        variables.definitions = definitions
        variables.client = client
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
        assertTrue(player.variables.data.isEmpty())
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
    fun `Get variable`() {
        // Given
        map[key] = 42
        // When
        val result = variables.get(key, -1)
        // Then
        assertEquals(42, result)
    }

    @Test
    fun `Get doesn't return default value`() {
        // Given
        every { variable.defaultValue } returns 42
        // When
        val result: Int? = variables.get(key)
        // Then
        assertNull(result)
    }

    @Test
    fun `Get no variable`() {
        // Given
        every { definitions.get(key) } returns null
        // When
        val result = variables.get(key, -1)
        // Then
        assertEquals(-1, result)
    }

    @Test
    fun `Add bitwise`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        map[key] = arrayListOf<Any>()
        every { definitions.get(key) } returns variable
        // When
        assertTrue(variables.bits.set(key, "First", true))
        // Then
        assertEquals(arrayListOf("First"), map[key])
        verify {
            variables.send(key)
            events.emit(VariableBitAdded(key, "First"))
        }
    }

    @Test
    fun `Add bitwise two`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        map[key] = arrayListOf("First")
        every { definitions.get(key) } returns variable
        // When
        assertTrue(variables.bits.set(key, "Second", true))
        // Then
        assertEquals(arrayListOf("First", "Second"), map[key])
        verify {
            variables.send(key)
            events.emit(VariableBitAdded(key, "Second"))
        }
    }

    @Test
    fun `Add bitwise existing`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        map[key] = arrayListOf("First")
        every { definitions.get(key) } returns variable
        // When
        assertFalse(variables.bits.set(key, "First", true))
        // Then
        assertEquals(arrayListOf("First"), map[key]) //Doesn't change
        verify(exactly = 0) { variables.send(key) }
    }

    @Test
    fun `Add bitwise no refresh`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        map[key] = arrayListOf<Any>()
        every { definitions.get(key) } returns variable
        // When
        assertTrue(variables.bits.set(key, "First", false))
        // Then
        assertEquals(arrayListOf("First"), map[key])
        verify(exactly = 0) { variables.send(key) }
    }

    @Test
    fun `Remove bitwise`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        map[key] = arrayListOf("First")
        every { definitions.get(key) } returns variable
        // When
        assertTrue(variables.bits.remove(key, "First", true))
        // Then
        assertEquals(emptyList<Any>(), map[key])
        verify {
            variables.send(key)
            events.emit(VariableBitRemoved(key, "First"))
        }
    }

    @Test
    fun `Remove bitwise no refresh`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        map[key] = arrayListOf("First")
        every { definitions.get(key) } returns variable
        // When
        assertTrue(variables.bits.remove(key, "First", false))
        // Then
        assertEquals(emptyList<Any>(), map[key])
        verify(exactly = 0) { variables.send(key) }
    }

    @Test
    fun `Persistence uses different variable map`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, false, transmit)
        variables.temp[key] = arrayListOf("First")
        every { definitions.get(key) } returns variable
        // When
        assertTrue(variables.bits.remove(key, "First", false))
        // Then
        assertEquals(emptyList<Any>(), variables.temp[key])
        verify(exactly = 0) { variables.send(key) }
    }

    @Test
    fun `Clear bitwise of multiple values`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, arrayListOf<Any>(), persist, transmit)
        map[key] = arrayListOf("Third")
        every { definitions.get(key) } returns variable
        // When
        variables.clear(key, true)
        // Then
        assertNull(map[key])
        verifyOrder {
            variables.send(key)
            events.emit(VariableSet(key, arrayListOf("Third"), null))
        }
    }

    companion object {
        private const val key = "key"
    }
}