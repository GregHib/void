package rs.dusk.engine.client.variable

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerVariables
import rs.dusk.network.codec.game.encode.sendVarbit
import rs.dusk.network.codec.game.encode.sendVarc
import rs.dusk.network.codec.game.encode.sendVarcStr
import rs.dusk.network.codec.game.encode.sendVarp

internal class VariablesTest {

    private lateinit var system: Variables
    private lateinit var component: PlayerVariables
    private lateinit var variable: Variable<Int>
    private lateinit var player: Player
    val key = "key"

    @BeforeEach
    fun setup() {
        variable = mockk(relaxed = true)
        every { variable.hash } returns 54321
        system = spyk(Variables())
        component = mutableMapOf()
        player = mockk(relaxed = true)
        mockkStatic("rs.dusk.network.rs.codec.game.encode.VarpMessageEncoderKt")
        every { player.sendVarp(any(), any()) } just Runs
        mockkStatic("rs.dusk.network.rs.codec.game.encode.VarbitMessageEncoderKt")
        every { player.sendVarbit(any(), any()) } just Runs
        mockkStatic("rs.dusk.network.rs.codec.game.encode.VarcMessageEncoderKt")
        every { player.sendVarc(any(), any()) } just Runs
        mockkStatic("rs.dusk.network.rs.codec.game.encode.VarcStrMessageEncoderKt")
        every { player.sendVarcStr(any(), any()) } just Runs
        every { player.variables } returns component
        setVariable(key, variable)
    }

    private fun setVariable(key: String, variable: Variable<*>) {
        system.names[key] = variable.hash
        system.variables[variable.hash] = variable
    }

    @Test
    fun `Set value`() {
        //Given
        setVariable(key, variable)
        val map = mutableMapOf<Int, Any>(variable.hash to 1)
        every { player.variables } returns map
        every { system.send(any(), any()) } just Runs
        //When
        system.set(player, key, 42, true)
        //Then
        assertEquals(42, map[variable.hash])
        verify { system.send(any(), any()) }
    }

    @Test
    fun `Set removes default value`() {
        //Given
        every { variable.defaultValue } returns 42
        val map = mutableMapOf<Int, Any>(variable.hash to 1)
        every { player.variables } returns map
        every { system.send(any(), any()) } just Runs
        //When
        system.set(player, key, 42, true)
        //Then
        assertTrue(player.variables.isEmpty())
        verify { system.send(any(), any()) }
    }

    @Test
    fun `Set value no refresh`() {
        //Given
        val map = mutableMapOf<Int, Any>(variable.hash to 1)
        every { player.variables } returns map
        every { system.send(any(), any()) } just Runs
        //When
        system.set(player, key, 42, false)
        //Then
        assertEquals(42, map[variable.hash])
        verify(exactly = 0) { system.send(any(), any()) }
    }

    @Test
    fun `Send varp`() {
        //Given
        every { variable.type } returns Variable.Type.VARP
        //When
        system.send(player, key)
        //Then
        verify { player.sendVarp(variable.id, 0) }
    }

    @Test
    fun `Send varbit`() {
        //Given
        every { variable.type } returns Variable.Type.VARBIT
        //When
        system.send(player, key)
        //Then
        verify { player.sendVarbit(variable.id, 0) }
    }

    @Test
    fun `Send varc`() {
        //Given
        every { variable.type } returns Variable.Type.VARC
        //When
        system.send(player, key)
        //Then
        verify { player.sendVarc(variable.id, 0) }
    }

    @Test
    fun `Send varcstr`() {
        //Given
        val variable = mockk<Variable<String>>(relaxed = true)
        every { variable.type } returns Variable.Type.VARCSTR
        every { variable.defaultValue } returns "nothing"
        setVariable(key, variable)
        //When
        system.send(player, key)
        //Then
        verify { player.sendVarcStr(variable.id, "nothing") }
    }

    @Test
    fun `Get variable`() {
        //Given
        every { player.variables } returns mutableMapOf(variable.hash to 42)
        //When
        val result = system.get(player, key, -1)
        //Then
        assertEquals(42, result)
    }

    @Test
    fun `Get default value`() {
        //Given
        every { variable.defaultValue } returns 42
        //When
        val result = system.get(player, key, -1)
        //Then
        assertEquals(42, result)
    }

    @Test
    fun `Get no variable`() {
        //Given
        system.variables.clear()
        //When
        val result = system.get(player, key, -1)
        //Then
        assertEquals(-1, result)
    }

    @Test
    fun `Add bitwise`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"))
        val map = mutableMapOf<Int, Any>(variable.hash to 0)
        setVariable(key, variable)
        every { player.variables } returns map
        //When
        system.add(player, key, "First", true)
        //Then
        assertEquals(1, map[variable.hash])
        verify{ system.send(player, key) }
    }

    @Test
    fun `Add bitwise two`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"))
        val map = mutableMapOf<Int, Any>(variable.hash to 1)
        setVariable(key, variable)
        every { player.variables } returns map
        //When
        system.add(player, key, "Second", true)
        //Then
        assertEquals(3, map[variable.hash])
        verify{ system.send(player, key) }
    }

    @Test
    fun `Add bitwise existing`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"))
        val map = mutableMapOf<Int, Any>(variable.hash to 1)
        setVariable(key, variable)
        every { player.variables } returns map
        //When
        system.add(player, key, "First", true)
        //Then
        assertEquals(1, map[variable.hash])//Doesn't change
        verify(exactly = 0) { system.send(player, key) }
    }

    @Test
    fun `Add bitwise no refresh`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"))
        val map = mutableMapOf<Int, Any>(variable.hash to 0)
        setVariable(key, variable)
        every { player.variables } returns map
        //When
        system.add(player, key, "First", false)
        //Then
        assertEquals(1, map[variable.hash])
        verify(exactly = 0) { system.send(player, key) }
    }

    @Test
    fun `Remove bitwise`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"))
        val map = mutableMapOf<Int, Any>(variable.hash to 3)
        setVariable(key, variable)
        every { player.variables } returns map
        //When
        system.remove(player, key, "First", true)
        //Then
        assertEquals(2, map[variable.hash])
        verify { system.send(player, key) }
    }

    @Test
    fun `Remove bitwise no refresh`() {
        //Given
        val variable = BitwiseVariable(0, Variable.Type.VARP, values = listOf("First", "Second"))
        val map = mutableMapOf<Int, Any>(variable.hash to 3)
        setVariable(key, variable)
        every { player.variables } returns map
        //When
        system.remove(player, key, "First", false)
        //Then
        assertEquals(2, map[variable.hash])
        verify(exactly = 0) { system.send(player, key) }
    }
}