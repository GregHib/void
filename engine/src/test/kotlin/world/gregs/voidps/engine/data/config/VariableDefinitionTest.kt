package world.gregs.voidps.engine.data.config

import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.VariableValues
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.protocol.encode.sendVarbit
import world.gregs.voidps.network.login.protocol.encode.sendVarc
import world.gregs.voidps.network.login.protocol.encode.sendVarcStr
import world.gregs.voidps.network.login.protocol.encode.sendVarp

internal class VariableDefinitionTest {

    private lateinit var client: Client

    private val id = 0
    private val default = 0
    private val persist = true
    private val transmit = true
    private val values = VariableValues(listOf("First", "Second"), "int", default)

    @BeforeEach
    fun setup() {
        client = mockk(relaxed = true)
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.VarpEncoderKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.VarbitEncoderKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.VarcEncoderKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.VarcStrEncoderKt")
    }

    @Test
    fun `Variable can prevent transmission`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, false)
        // When
        variable.send(client, 0)
        // Then
        verify(exactly = 0) { client.sendVarc(any(), any()) }
    }

    @Test
    fun `Send varp`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(id, values, default, persist, transmit)
        // When
        variable.send(client, 0)
        // Then
        verify { client.sendVarp(variable.id, 0) }
    }

    @Test
    fun `Send varbit`() {
        val variable = VariableDefinition.VarbitDefinition(id, values, default, persist, transmit)
        // When
        variable.send(client, 0)
        // Then
        verify { client.sendVarbit(variable.id, 0) }
    }

    @Test
    fun `Send varc`() {
        // Given
        val variable = VariableDefinition.VarcDefinition(id, values, default, persist, transmit)
        // When
        variable.send(client, 0)
        // Then
        verify { client.sendVarc(variable.id, 0) }
    }

    @Test
    fun `Send varcstr`() {
        // Given
        val variable = VariableDefinition.VarcStrDefinition(id, VariableValues(listOf("First", "Second"), "string", default), persist, transmit)
        // When
        variable.send(client, "nothing")
        // Then
        verify { client.sendVarcStr(variable.id, "nothing") }
    }
}
