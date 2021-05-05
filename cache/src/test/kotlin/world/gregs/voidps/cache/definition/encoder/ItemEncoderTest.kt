package world.gregs.voidps.cache.definition.encoder

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.definition.data.ItemDefinition
import java.io.File

/**
 * @author GregHib <greg@gregs.world>
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
            floorScaleZ = 175,
            floorScaleY = 176,
            ambience = 120,
            diffusion = 75,
            team = 2,
            lendId = 150,
            lendTemplateId = 72,
            maleWieldX = 12,
            maleWieldZ = 13,
            maleWieldY = 17,
            femaleWieldX = 11,
            femaleWieldZ = 13,
            femaleWieldY = 21,
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

        val writer = BufferWriter(1024)
        with(encoder) {
            writer.encode(definition)
        }

        val data = writer.array()

        val file = File("item-definition.dat")
        file.writeBytes(data)
        val stream = file.inputStream()
        val expected = stream.readAllBytes()
        assertArrayEquals(expected, data)
    }
}