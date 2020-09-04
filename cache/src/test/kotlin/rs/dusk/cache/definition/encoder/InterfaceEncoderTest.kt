package rs.dusk.cache.definition.encoder

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.cache.Cache
import rs.dusk.cache.Indices.INTERFACES
import rs.dusk.cache.definition.data.InterfaceComponentDefinition
import rs.dusk.cache.definition.data.InterfaceComponentSetting
import rs.dusk.cache.definition.decoder.InterfaceDecoder
import rs.dusk.core.io.write.BufferWriter

internal class InterfaceEncoderTest {

    @Test
    fun `Encode full test`() {
        val definition = InterfaceComponentDefinition(
            id = 0,
            type = 1,
            unknown = "Bob",
            contentType = 1,
            basePositionX = 1000,
            basePositionY = 1500,
            baseWidth = 400,
            baseHeight = 600,
            horizontalSizeMode = 1,
            verticalSizeMode = 2,
            horizontalPositionMode = 2,
            verticalPositionMode = 1,
            parent = 1,
            hidden = true,
            keyRepeat = null,
            keyCodes = null,
            keyModifiers = null,
            name = "Applied",
            options = arrayOf("One", "Two", "Three"),
            mouseIcon = intArrayOf(123),
            optionOverride = "A string",
            anInt4708 = 1,
            anInt4795 = 2,
            anInt4860 = 3,
            useOption = "Second",
            setting = InterfaceComponentSetting(1234 or 0x3fda8, 567),
            anInt4698 = 8910,
            anInt4839 = 1112,
            anObjectArray4758 = arrayOf(1, "two", 3, "four"),
            clientVarc = intArrayOf(5, 4, 3, 2, 1),
            hasScript = true
        )
        val encoder = InterfaceEncoder()

        val writer = BufferWriter()
        with(encoder) {
            writer.encode(definition)
        }

        val data = writer.buffer.array().copyOf(writer.position())

        val cache: Cache = mockk(relaxed = true)
        every { cache.getFile(INTERFACES, any(), any<Int>()) } returns data
        val decoder = InterfaceDecoder(cache)
        val inter = decoder.get(0)
        val decoded = inter.components?.get(0)
        assertEquals(definition, decoded)
    }
}