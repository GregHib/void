package world.gregs.voidps.engine.client.variable

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.encode.sendVarbit
import world.gregs.voidps.network.encode.sendVarc
import world.gregs.voidps.network.encode.sendVarcStr
import world.gregs.voidps.network.encode.sendVarp

internal class VariablesTest {

    private lateinit var manager: VariableManager
    private lateinit var variables: Variables
    private lateinit var component: MutableMap<String, Any>
    private lateinit var variable: Variable<Int>
    private lateinit var player: Player
    private lateinit var map: MutableMap<String, Any>

    @BeforeEach
    fun setup() {
        map = mutableMapOf()
        variable = mockk(relaxed = true)
        every { variable.persistent } returns true
        manager = mockk(relaxed = true)
        component = mutableMapOf()
        variables = spyk(Variables(map))
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
        every { manager.get(key) } returns variable
        variables.player = player
        variables.manager = manager
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
        verify { variables.send(any()) }
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
        every { manager.get(key) } returns variable
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
        every { manager.get(key) } returns null
        // Given
        manager.clear()
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
        every { manager.get(key) } returns variable
        // When
        variables.add(key, "First", true)
        // Then
        assertEquals(1, map[key])
        verify{ variables.send(key) }
    }

    @Test
    fun `Add bitwise two`() {
        // Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        map[key] = 1
        every { manager.get(key) } returns variable
        // When
        variables.add(key, "Second", true)
        // Then
        assertEquals(3, map[key])
        verify{ variables.send(key) }
    }

    @Test
    fun `Add bitwise existing`() {
        // Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        map[key] = 1
        every { manager.get(key) } returns variable
        // When
        variables.add(key, "First", true)
        // Then
        assertEquals(1, map[key])//Doesn't change
        verify(exactly = 0) { variables.send(key) }
    }

    @Test
    fun `Add bitwise no refresh`() {
        // Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        map[key] = 0
        every { manager.get(key) } returns variable
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
        every { manager.get(key) } returns variable
        // When
        variables.remove(key, "First", true)
        // Then
        assertEquals(2, map[key])
        verify { variables.send(key) }
    }

    @Test
    fun `Remove bitwise no refresh`() {
        // Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        map[key] = 3
        every { manager.get(key) } returns variable
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
        every { manager.get(key) } returns variable
        // When
        variables.remove(key, "First", false)
        // Then
        assertEquals(2, variables.temporaryVariables[key])
        verify(exactly = 0) { variables.send(key) }
    }

    companion object {
        private const val key = "key"
    }
}