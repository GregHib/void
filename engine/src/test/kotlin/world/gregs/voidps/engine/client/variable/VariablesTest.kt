package world.gregs.voidps.engine.client.variable

import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.Client

internal class VariablesTest {

    private lateinit var definitions: VariableDefinitions
    private lateinit var variables: PlayerVariables
    private lateinit var variable: VariableDefinition
    private lateinit var player: Player
    private lateinit var client: Client
    private lateinit var map: MutableMap<String, Any>
    private lateinit var calls: MutableList<Pair<Any?, Any?>>

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
        player = mockk(relaxed = true)
        variables = spyk(PlayerVariables(player, map))
        variables.bits = VariableBits(variables, player)
        client = mockk(relaxed = true)
        every { player.variables } returns variables
        every { definitions.get(KEY) } returns variable
        variables.definitions = definitions
        variables.client = client
        calls = mutableListOf()
        object : VariableApi {
            init {
                variableSet { id, from, to ->
                    assertEquals(player, player)
                    assertEquals(KEY, id)
                    calls.add(from to to)
                }
            }
        }
        mockkObject(VariableApi)
    }

    @AfterEach
    fun teardown() {
        VariableApi.close()
        unmockkObject(VariableApi)
    }

    @Test
    fun `Set value`() {
        // Given
        map[KEY] = 1
        every { variables.send(any()) } just Runs
        // When
        variables.set(KEY, 42, true)
        // Then
        assertEquals(42, map[KEY])
        verify {
            variables.send(any())
        }
        assertEquals(1 to 42, calls.first())
    }

    @Test
    fun `Set removes default value`() {
        // Given
        every { variable.defaultValue } returns 42
        map[KEY] = 1
        every { variables.send(any()) } just Runs
        // When
        variables.set(KEY, 42, true)
        // Then
        assertTrue(player.variables.data.isEmpty())
        verify { variables.send(any()) }
    }

    @Test
    fun `Set value no refresh`() {
        // Given
        map[KEY] = 1
        every { variables.send(any()) } just Runs
        // When
        variables.set(KEY, 42, false)
        // Then
        assertEquals(42, map[KEY])
        verify(exactly = 0) { variables.send(any()) }
    }

    @Test
    fun `Get variable`() {
        // Given
        map[KEY] = 42
        // When
        val result = variables.get(KEY, -1)
        // Then
        assertEquals(42, result)
    }

    @Test
    fun `Get doesn't return default value`() {
        // Given
        every { variable.defaultValue } returns 42
        // When
        val result: Int? = variables.get(KEY)
        // Then
        assertNull(result)
    }

    @Test
    fun `Get no variable`() {
        // Given
        every { definitions.get(KEY) } returns null
        // When
        val result = variables.get(KEY, -1)
        // Then
        assertEquals(-1, result)
    }

    @Test
    fun `Add bitwise`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        map[KEY] = arrayListOf<Any>()
        every { definitions.get(KEY) } returns variable
        // When
        assertTrue(variables.bits.set(KEY, "First", true))
        // Then
        assertEquals(arrayListOf("First"), map[KEY])
        verify {
            variables.send(KEY)
            VariableApi.add(player, KEY, "First")
        }
    }

    @Test
    fun `Add bitwise two`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        map[KEY] = arrayListOf("First")
        every { definitions.get(KEY) } returns variable
        // When
        assertTrue(variables.bits.set(KEY, "Second", true))
        // Then
        assertEquals(arrayListOf("First", "Second"), map[KEY])
        verify {
            variables.send(KEY)
            VariableApi.add(player, KEY, "Second")
        }
    }

    @Test
    fun `Add bitwise existing`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        map[KEY] = arrayListOf("First")
        every { definitions.get(KEY) } returns variable
        // When
        assertFalse(variables.bits.set(KEY, "First", true))
        // Then
        assertEquals(arrayListOf("First"), map[KEY]) // Doesn't change
        verify(exactly = 0) { variables.send(KEY) }
    }

    @Test
    fun `Add bitwise no refresh`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        map[KEY] = arrayListOf<Any>()
        every { definitions.get(KEY) } returns variable
        // When
        assertTrue(variables.bits.set(KEY, "First", false))
        // Then
        assertEquals(arrayListOf("First"), map[KEY])
        verify(exactly = 0) { variables.send(KEY) }
    }

    @Test
    fun `Remove bitwise`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        map[KEY] = arrayListOf("First")
        every { definitions.get(KEY) } returns variable
        // When
        assertTrue(variables.bits.remove(KEY, "First", true))
        // Then
        assertEquals(emptyList<Any>(), map[KEY])
        verify {
            variables.send(KEY)
            VariableApi.remove(player, KEY, "First")
        }
    }

    @Test
    fun `Remove bitwise no refresh`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        map[KEY] = arrayListOf("First")
        every { definitions.get(KEY) } returns variable
        // When
        assertTrue(variables.bits.remove(KEY, "First", false))
        // Then
        assertEquals(emptyList<Any>(), map[KEY])
        verify(exactly = 0) { variables.send(KEY) }
    }

    @Test
    fun `Persistence uses different variable map`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, false, transmit)
        variables.temp[KEY] = arrayListOf("First")
        every { definitions.get(KEY) } returns variable
        // When
        assertTrue(variables.bits.remove(KEY, "First", false))
        // Then
        assertEquals(emptyList<Any>(), variables.temp[KEY])
        verify(exactly = 0) { variables.send(KEY) }
    }

    @Test
    fun `Clear bitwise of multiple values`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, arrayListOf<Any>(), persist, transmit)
        map[KEY] = arrayListOf("Third")
        every { definitions.get(KEY) } returns variable
        // When
        variables.clear(KEY, true)
        // Then
        assertNull(map[KEY])
        verifyOrder {
            variables.send(KEY)
        }
        assertEquals(arrayListOf("Third") to null, calls.first())
    }

    companion object {
        private const val KEY = "key"
    }
}
