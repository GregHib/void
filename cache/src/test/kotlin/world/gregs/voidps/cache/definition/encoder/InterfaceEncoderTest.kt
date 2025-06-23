package world.gregs.voidps.cache.definition.encoder

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index.INTERFACES
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinitionFull
import world.gregs.voidps.cache.definition.data.InterfaceComponentSetting
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoderFull

internal class InterfaceEncoderTest {

    @Test
    fun `Encode full test`() {
        val definition = InterfaceComponentDefinitionFull(
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
            keyRepeats = null,
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
            information = arrayOf(1, "two", 3, "four"),
            clientVarc = intArrayOf(5, 4, 3, 2, 1),
            hasScript = true,
        )
        val encoder = InterfaceEncoder()

        val writer = BufferWriter(1024)
        with(encoder) {
            writer.encode(definition)
        }

        val data = writer.toArray()

        val cache: Cache = mockk(relaxed = true)
        every { cache.data(INTERFACES, any(), any<Int>()) } returns data
        every { cache.lastArchiveId(any()) } returns 1
        val decoder = InterfaceDecoderFull().load(cache)
        val inter = decoder[0]
        val decoded = inter.components?.get(0)
        assertEquals(definition, decoded)
    }
}
