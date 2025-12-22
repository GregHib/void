package world.gregs.voidps.cache.type.decode

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import java.io.BufferedInputStream
import java.io.StringWriter

class ItemTypeDecoderTest {

    @Test
    fun `Encode binary test`() {
        val decoder = ItemTypeDecoder(1)
        fill(decoder)

        val writer = ArrayWriter(1024)
        decoder.writePacked(writer, 0)

        val newDecoder = ItemTypeDecoder(1)
        val reader = ArrayReader(writer.array())
        newDecoder.readPacked(reader, 0)
        newDecoder.id.set(0, 10)
        newDecoder.stringId.set(0, "item_id")

        assertEquals(decoder, newDecoder)
    }

    @Test
    fun `Encode config test`() {
        val decoder = ItemTypeDecoder(1)
        fill(decoder)

        val writer = StringWriter()
        decoder.writeConfig(writer, 0)

        val string = writer.toString()

        println(string)

        val newDecoder = ItemTypeDecoder(1)
        val reader = ConfigReader(BufferedInputStream(string.byteInputStream()))
        newDecoder.readConfig(reader, 0)
        newDecoder.id.set(0, 10)

        assertEquals(decoder, newDecoder)
    }

    private fun fill(decoder: ItemTypeDecoder) {
        val index = 0
        decoder.id.set(index, 10)
        decoder.stringId.set(index, "item_id")
        decoder.modelId.set(index, 13450)
        decoder.name.set(index, "Not a null")
        decoder.spriteScale.set(index, 780)
        decoder.spritePitch.set(index, 150)
        decoder.spriteCameraRoll.set(index, 2)
        decoder.spriteTranslateX.set(index, 15)
        decoder.spriteTranslateY.set(index, 10)
        decoder.stackable.set(index, 1)
        decoder.cost.set(index, 750)
        decoder.members.set(index, true)
        decoder.multiStackSize.set(index, 1)
        decoder.primaryMaleModel.set(index, 1500)
        decoder.secondaryMaleModel.set(index, 38000)
        decoder.primaryFemaleModel.set(index, 10)
        decoder.secondaryFemaleModel.set(index, 41000)
        decoder.floorOptions.set(index, arrayOf("Examine", "Take", "Eat", "Stop", null, "Examine"))
        decoder.options.set(index, arrayOf(null, "Take", "Eat", "Stop", "Kick"))
        decoder.colours.setFirst(index, shortArrayOf(14000, 15000, 16000))
        decoder.colours.setSecond(index, shortArrayOf(14000, 15000, 16000))
        decoder.textureColours.setFirst(index, shortArrayOf(14000, 15000, 16000))
        decoder.textureColours.setSecond(index, shortArrayOf(14000, 15000, 16000))
        decoder.recolourPalette.set(index, byteArrayOf(0, 1, 2, 3, 4))
        decoder.exchangeable.set(index, true)
        decoder.tertiaryMaleModel.set(index, 13441)
        decoder.tertiaryFemaleModel.set(index, 2673)
        decoder.primaryMaleDialogueHead.set(index, 875)
        decoder.primaryFemaleDialogueHead.set(index, 924)
        decoder.secondaryMaleDialogueHead.set(index, 368)
        decoder.secondaryFemaleDialogueHead.set(index, 1390)
        decoder.spriteCameraYaw.set(index, 100)
        decoder.dummyItem.set(index, 212)
        decoder.noteId.set(index, 12)
        decoder.notedTemplateId.set(index, 15)
        decoder.stack.first[index] = shortArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
        decoder.stack.second[index] = shortArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        decoder.floorScaleX.set(index, 174)
        decoder.floorScaleZ.set(index, 175)
        decoder.floorScaleY.set(index, 176)
        decoder.ambience.set(index, 120)
        decoder.diffusion.set(index, 75)
        decoder.team.set(index, 2)
        decoder.lendId.set(index, 150)
        decoder.lendTemplateId.set(index, 72)
        decoder.maleWield.first.set(index, 8)
        decoder.maleWield.second.set(index, 12)
        decoder.maleWield.third.set(index, 16)
        decoder.femaleWield.first.set(index, 8)
        decoder.femaleWield.second.set(index, 12)
        decoder.femaleWield.third.set(index, 20)
        decoder.primaryCursor.first.set(index, 10)
        decoder.primaryCursor.second.set(index, 5)
        decoder.secondaryCursor.first.set(index, 7)
        decoder.secondaryCursor.second.set(index, 9)
        decoder.primaryInterfaceCursor.first.set(index, 3)
        decoder.primaryInterfaceCursor.second.set(index, 8)
        decoder.secondaryInterfaceCursor.first.set(index, 5)
        decoder.secondaryInterfaceCursor.second.set(index, 11)
        decoder.campaigns.set(index, shortArrayOf(1, 5, 100))
        decoder.pickSizeShift.set(index, 2)
        decoder.singleNoteId.set(index, 159)
        decoder.singleNoteTemplateId.set(index, 179)
        decoder.parameters.set(index, mutableMapOf(1 to "string", 2 to 100000))
    }
}
