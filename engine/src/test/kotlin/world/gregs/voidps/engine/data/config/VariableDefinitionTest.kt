package world.gregs.voidps.engine.data.config

import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.encode.sendVarbit
import world.gregs.voidps.network.encode.sendVarc
import world.gregs.voidps.network.encode.sendVarcStr
import world.gregs.voidps.network.encode.sendVarp

internal class VariableDefinitionTest {

    private lateinit var defs: MutableMap<String, Any>
    private lateinit var client: Client

    @BeforeEach
    fun setup() {
        defs = mutableMapOf(
            "id" to 0,
            "format" to "int",
            "persist" to true,
            "transmit" to true,
            "values" to listOf("First", "Second")
        )
        client = mockk(relaxed = true)
        mockkStatic("world.gregs.voidps.network.encode.VarpEncoderKt")
        mockkStatic("world.gregs.voidps.network.encode.VarbitEncoderKt")
        mockkStatic("world.gregs.voidps.network.encode.VarcEncoderKt")
        mockkStatic("world.gregs.voidps.network.encode.VarcStrEncoderKt")
    }

    @Test
    fun `Variable can prevent transmission`() {
        // Given
        defs["transmit"] = false
        val variable = VariableDefinition.VarpDefinition(defs)
        // When
        variable.send(client, 0)
        // Then
        verify(exactly = 0) { client.sendVarc(any(), any()) }
    }

    @Test
    fun `Send varp`() {
        // Given
        val variable = VariableDefinition.VarpDefinition(defs)
        // When
        variable.send(client, 0)
        // Then
        verify { client.sendVarp(variable.id, 0) }
    }

    @Test
    fun `Send varbit`() {
        val variable = VariableDefinition.VarbitDefinition(defs)
        // When
        variable.send(client, 0)
        // Then
        verify { client.sendVarbit(variable.id, 0) }
    }

    @Test
    fun `Send varc`() {
        // Given
        val variable = VariableDefinition.VarcDefinition(defs)
        // When
        variable.send(client, 0)
        // Then
        verify { client.sendVarc(variable.id, 0) }
    }

    @Test
    fun `Send varcstr`() {
        // Given
        defs["format"] = "string"
        val variable = VariableDefinition.VarcStrDefinition(defs)
        // When
        variable.send(client, "nothing")
        // Then
        verify { client.sendVarcStr(variable.id, "nothing") }
    }
}