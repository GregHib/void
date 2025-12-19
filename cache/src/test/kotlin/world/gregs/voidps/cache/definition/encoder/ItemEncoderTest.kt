package world.gregs.voidps.cache.definition.encoder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ItemDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ItemDecoderFull
import java.nio.ByteBuffer

internal class ItemEncoderTest {

    @Test
    fun `Encode full test`() {
        val definition = ItemDefinitionFull(
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
            maleWieldX = 8,
            maleWieldZ = 12,
            maleWieldY = 16,
            femaleWieldX = 8,
            femaleWieldZ = 12,
            femaleWieldY = 20,
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
            params = hashMapOf(1 to "string", 2 to 100000),
        )
        val encoder = ItemEncoder()

        val writer = BufferWriter(1024)
        with(encoder) {
            writer.encode(definition)
        }

        val data = writer.array()

        val decoder = ItemDecoderFull()
        val decodedDefinition = ItemDefinitionFull(id = definition.id)
        val reader = ArrayReader(ByteBuffer.wrap(data))
        decoder.readLoop(decodedDefinition, reader)

        assertEquals(definition, decodedDefinition)
    }

    @Disabled
    @Test
    fun `Encode everything`() {
        val cache: Cache = CacheDelegate("../data/cache/")
        val decoder = ItemDecoderFull()
        val full = decoder.load(cache)
        val encoder = ItemEncoder()
        val writer = BufferWriter(1024)

        for (definition in full) {
            with(encoder) {
                writer.clear()
                writer.encode(definition)
            }
            val data = writer.array()
            val decodedDefinition = ItemDefinitionFull(id = definition.id)
            val reader = ArrayReader(ByteBuffer.wrap(data))
            decoder.readLoop(decodedDefinition, reader)
            assertEquals(definition, decodedDefinition)
        }
    }
}
