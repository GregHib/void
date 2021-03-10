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

    private lateinit var variables: Variables
    private lateinit var component: MutableMap<String, Any>
    private lateinit var variable: Variable<Int>
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        variable = mockk(relaxed = true)
        every { variable.persistent } returns true
        variables = spyk(Variables())
        component = mutableMapOf()
        player = mockk(relaxed = true)
        mockkStatic("world.gregs.voidps.network.encode.VarpEncoderKt")
        every { player.sendVarp(any(), any()) } just Runs
        mockkStatic("world.gregs.voidps.network.encode.VarbitEncoderKt")
        every { player.sendVarbit(any(), any()) } just Runs
        mockkStatic("world.gregs.voidps.network.encode.VarcEncoderKt")
        every { player.sendVarc(any(), any()) } just Runs
        mockkStatic("world.gregs.voidps.network.encode.VarcStrEncoderKt")
        every { player.sendVarcStr(any(), any()) } just Runs
        every { player.variables } returns component
        variables.register(key, variable)
    }

    @Test
    fun `Set value`() {
        //Given
        variables.register(key, variable)
        val map = mutableMapOf<String, Any>(key to 1)
        every { player.variables } returns map
        every { variables.send(any(), any()) } just Runs
        //When
        variables.set(player, key, 42, true)
        //Then
        assertEquals(42, map[key])
        verify { variables.send(any(), any()) }
    }

    @Test
    fun `Set removes default value`() {
        //Given
        every { variable.defaultValue } returns 42
        val map = mutableMapOf<String, Any>(key to 1)
        every { player.variables } returns map
        every { variables.send(any(), any()) } just Runs
        //When
        variables.set(player, key, 42, true)
        //Then
        assertTrue(player.variables.isEmpty())
        verify { variables.send(any(), any()) }
    }

    @Test
    fun `Set value no refresh`() {
        //Given
        val map = mutableMapOf<String, Any>(key to 1)
        every { player.variables } returns map
        every { variables.send(any(), any()) } just Runs
        //When
        variables.set(player, key, 42, false)
        //Then
        assertEquals(42, map[key])
        verify(exactly = 0) { variables.send(any(), any()) }
    }

    @Test
    fun `Send varp`() {
        //Given
        every { variable.type } returns Variable.Type.VARP
        //When
        variables.send(player, key)
        //Then
        verify { player.sendVarp(variable.id, 0) }
    }

    @Test
    fun `Send varbit`() {
        //Given
        every { variable.type } returns Variable.Type.VARBIT
        //When
        variables.send(player, key)
        //Then
        verify { player.sendVarbit(variable.id, 0) }
    }

    @Test
    fun `Send varc`() {
        //Given
        every { variable.type } returns Variable.Type.VARC
        //When
        variables.send(player, key)
        //Then
        verify { player.sendVarc(variable.id, 0) }
    }

    @Test
    fun `Send varcstr`() {
        //Given
        val variable = mockk<Variable<String>>(relaxed = true)
        every { variable.type } returns Variable.Type.VARCSTR
        every { variable.defaultValue } returns "nothing"
        variables.register(key, variable)
        //When
        variables.send(player, key)
        //Then
        verify { player.sendVarcStr(variable.id, "nothing") }
    }

    @Test
    fun `Get variable`() {
        //Given
        every { player.variables } returns mutableMapOf(key to 42)
        //When
        val result = variables.get(player, key, -1)
        //Then
        assertEquals(42, result)
    }

    @Test
    fun `Get default value`() {
        //Given
        every { variable.defaultValue } returns 42
        //When
        val result = variables.get(player, key, -1)
        //Then
        assertEquals(42, result)
    }

    @Test
    fun `Get no variable`() {
        //Given
        variables.clear()
        //When
        val result = variables.get(player, key, -1)
        //Then
        assertEquals(-1, result)
    }

    @Test
    fun `Add bitwise`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        val map = mutableMapOf<String, Any>(key to 0)
        variables.register(key, variable)
        every { player.variables } returns map
        //When
        variables.add(player, key, "First", true)
        //Then
        assertEquals(1, map[key])
        verify{ variables.send(player, key) }
    }

    @Test
    fun `Add bitwise two`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        val map = mutableMapOf<String, Any>(key to 1)
        variables.register(key, variable)
        every { player.variables } returns map
        //When
        variables.add(player, key, "Second", true)
        //Then
        assertEquals(3, map[key])
        verify{ variables.send(player, key) }
    }

    @Test
    fun `Add bitwise existing`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        val map = mutableMapOf<String, Any>(key to 1)
        variables.register(key, variable)
        every { player.variables } returns map
        //When
        variables.add(player, key, "First", true)
        //Then
        assertEquals(1, map[key])//Doesn't change
        verify(exactly = 0) { variables.send(player, key) }
    }

    @Test
    fun `Add bitwise no refresh`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        val map = mutableMapOf<String, Any>(key to 0)
        variables.register(key, variable)
        every { player.variables } returns map
        //When
        variables.add(player, key, "First", false)
        //Then
        assertEquals(1, map[key])
        verify(exactly = 0) { variables.send(player, key) }
    }

    @Test
    fun `Remove bitwise`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        val map = mutableMapOf<String, Any>(key to 3)
        variables.register(key, variable)
        every { player.variables } returns map
        //When
        variables.remove(player, key, "First", true)
        //Then
        assertEquals(2, map[key])
        verify { variables.send(player, key) }
    }

    @Test
    fun `Remove bitwise no refresh`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = true)
        val map = mutableMapOf<String, Any>(key to 3)
        variables.register(key, variable)
        every { player.variables } returns map
        //When
        variables.remove(player, key, "First", false)
        //Then
        assertEquals(2, map[key])
        verify(exactly = 0) { variables.send(player, key) }
    }

    @Test
    fun `Persistence uses different variable map`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"), persistent = false)
        val map = mutableMapOf<String, Any>(key to 3)
        variables.register(key, variable)
        every { player.temporaryVariables } returns map
        //When
        variables.remove(player, key, "First", false)
        //Then
        assertEquals(2, map[key])
        verify(exactly = 0) { variables.send(player, key) }
    }

    companion object {
        private const val key = "key"
    }
}