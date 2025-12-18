package world.gregs.voidps.cache.type.decode

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import java.io.BufferedInputStream
import java.io.StringWriter

class ItemTypeDecoderTest {

    @Test
    fun `Encode binary test`() {
        val decoder = ItemTypeDecoder()
        fill(decoder)

        val writer = BufferWriter(1024)
        decoder.writeBinary(writer)

        val newDecoder = ItemTypeDecoder()
        val reader = BufferReader(writer.array())
        newDecoder.readBinary(reader)
        newDecoder.id.value = 10
        newDecoder.stringId.value = "item_id"

        assertEquals(decoder, newDecoder)
    }

    @Test
    fun `Encode config test`() {
        val decoder = ItemTypeDecoder()
        fill(decoder)

        val writer = StringWriter()
        decoder.writeConfig(writer)

        val string = writer.toString()

        val newDecoder = ItemTypeDecoder()
        val reader = ConfigReader(BufferedInputStream(string.byteInputStream()))
        newDecoder.readConfig(reader)
        newDecoder.id.value = 10

        assertEquals(decoder, newDecoder)
    }

    private fun fill(decoder: ItemTypeDecoder) {
        decoder.id.value = 10
        decoder.stringId.value = "item_id"
        decoder.modelId.value = 13450
        decoder.name.value = "Not a null"
        decoder.spriteScale.value = 780
        decoder.spritePitch.value = 150
        decoder.spriteCameraRoll.value = 2
        decoder.spriteTranslateX.value = 15
        decoder.spriteTranslateY.value = 10
        decoder.stackable.value = 1
        decoder.cost.value = 750
        decoder.members.value = true
        decoder.multiStackSize.value = 1
        decoder.primaryMaleModel.value = 1500
        decoder.secondaryMaleModel.value = 38000
        decoder.primaryFemaleModel.value = 10
        decoder.secondaryFemaleModel.value = 41000
        decoder.floorOptions.value = arrayOf("Examine", "Take", "Eat", "Stop", null, "Examine")
        decoder.options.value = arrayOf(null, "Take", "Eat", "Stop", "Kick")
        decoder.colours.original = shortArrayOf(14000, 15000, 16000)
        decoder.colours.modified = shortArrayOf(14000, 15000, 16000)
        decoder.textureColours.original = shortArrayOf(14000, 15000, 16000)
        decoder.textureColours.modified = shortArrayOf(14000, 15000, 16000)
        decoder.recolourPalette.value = byteArrayOf(0, 1, 2, 3, 4)
        decoder.exchangeable.value = true
        decoder.tertiaryMaleModel.value = 13441
        decoder.tertiaryFemaleModel.value = 2673
        decoder.primaryMaleDialogueHead.value = 875
        decoder.primaryFemaleDialogueHead.value = 924
        decoder.secondaryMaleDialogueHead.value = 368
        decoder.secondaryFemaleDialogueHead.value = 1390
        decoder.spriteCameraYaw.value = 100
        decoder.dummyItem.value = 212
        decoder.noteId.value = 12
        decoder.notedTemplateId.value = 15
        decoder.stack.ids = intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
        decoder.stack.amounts = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        decoder.floorScaleX.value = 174
        decoder.floorScaleZ.value = 175
        decoder.floorScaleY.value = 176
        decoder.ambience.value = 120
        decoder.diffusion.value = 75
        decoder.team.value = 2
        decoder.lendId.value = 150
        decoder.lendTemplateId.value = 72
        decoder.maleWield.first.value = 8
        decoder.maleWield.second.value = 12
        decoder.maleWield.third.value = 16
        decoder.femaleWield.first.value = 8
        decoder.femaleWield.second.value = 12
        decoder.femaleWield.third.value = 20
        decoder.primaryCursor.first.value = 10
        decoder.primaryCursor.second.value = 5
        decoder.secondaryCursor.first.value = 7
        decoder.secondaryCursor.second.value = 9
        decoder.primaryInterfaceCursor.first.value = 3
        decoder.primaryInterfaceCursor.second.value = 8
        decoder.secondaryInterfaceCursor.first.value = 5
        decoder.secondaryInterfaceCursor.second.value = 11
        decoder.campaigns.value = shortArrayOf(1, 5, 100)
        decoder.pickSizeShift.value = 2
        decoder.singleNoteId.value = 159
        decoder.singleNoteTemplateId.value = 179
        decoder.parameters.value = mapOf<Int, Any>(1 to "string", 2 to 100000)
    }
}