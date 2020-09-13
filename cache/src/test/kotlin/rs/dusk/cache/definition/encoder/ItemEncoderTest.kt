package rs.dusk.cache.definition.encoder

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.core.io.write.BufferWriter

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
internal class ItemEncoderTest {

    @Test
    fun `Encode full test`() {
        val definition = ItemDefinition(
            id = 10,
            modelId = 13450,
            name = "Not a null",
            spriteScale = 780,
            spritePitch = 150,
            spriteCameraRoll = 2,
            spriteTranslateX = 15,
            spriteTranslateY = 10,
            stackable = 1,
            cost = 750,
            members = true,
            multiStackSize = 1,
            primaryMaleModel = 1500,
            secondaryMaleModel = 38000,
            primaryFemaleModel = 10,
            secondaryFemaleModel = 41000,
            floorOptions = arrayOf("Examine", "Take", "Eat", "Stop", "Kick", "Examine"),
            options = arrayOf("Drop", "Take", "Eat", "Stop", "Kick"),
            originalColours = shortArrayOf(14000, 15000, 16000),
            modifiedColours = shortArrayOf(14000, 15000, 16000),
            originalTextureColours = shortArrayOf(14000, 15000, 16000),
            modifiedTextureColours = shortArrayOf(14000, 15000, 16000),
            recolourPalette = byteArrayOf(0, 1, 2, 3, 4),
            exchangeable = true,
            tertiaryMaleModel = 13441,
            tertiaryFemaleModel = 2673,
            primaryMaleDialogueHead = 875,
            primaryFemaleDialogueHead = 924,
            secondaryMaleDialogueHead = 368,
            secondaryFemaleDialogueHead = 1390,
            spriteCameraYaw = 100,
            dummyItem = 212,
            noteId = 12,
            notedTemplateId = 15,
            stackIds = intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0),
            stackAmounts = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
            floorScaleX = 174,
            floorScaleY = 175,
            floorScaleZ = 176,
            ambience = 120,
            diffusion = 75,
            team = 2,
            lendId = 150,
            lendTemplateId = 72,
            maleWieldX = 12,
            maleWieldY = 13,
            maleWieldZ = 17,
            femaleWieldX = 11,
            femaleWieldY = 13,
            femaleWieldZ = 21,
            primaryCursorOpcode = 10,
            primaryCursor = 5,
            secondaryCursorOpcode = 7,
            secondaryCursor = 9,
            primaryInterfaceCursorOpcode = 3,
            primaryInterfaceCursor = 8,
            secondaryInterfaceCursorOpcode = 5,
            secondaryInterfaceCursor = 11,
            campaigns = intArrayOf(1, 5, 100),
            pickSizeShift = 2,
            singleNoteId = 159,
            singleNoteTemplateId = 179,
            params = hashMapOf(1L to "string", 2L to 100000)
        )
        val encoder = ItemEncoder()

        val writer = BufferWriter()
        with(encoder) {
            writer.encode(definition)
        }

        val data = writer.buffer.array()

//        val file = File("item-definition.dat")
//        file.writeBytes(data)
        val stream = ItemEncoderTest::class.java.getResourceAsStream("item-definition.dat")
        val expected = stream.readAllBytes()
        assertArrayEquals(expected, data)
    }
}