package world.gregs.voidps.cache.definition.encoder

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import java.io.File

internal class ObjectEncoderTest {

    @Test
    fun `Encode full test`() {
        val definition = ObjectDefinition(
            0,
            modelIds = arrayOf(intArrayOf(1, 2, 35000), intArrayOf(60000, 1, 2)),
            modelTypes = byteArrayOf(1, 2, 3),
            name = "Test Object",
            sizeX = 2,
            sizeY = 2,
            blocksSky = false,
            solid = 1,
            interactive = 0,
            contouredGround = 1,
            delayShading = true,
            offsetMultiplier = 128,
            brightness = 1,
            options = arrayOf("Take", "Eat", "Stop", "Kick", "Speak"),
            contrast = 1,
            originalColours = shortArrayOf(14000, 15000, 16000),
            modifiedColours = shortArrayOf(14000, 15000, 16000),
            originalTextureColours = shortArrayOf(14000, 15000, 16000),
            modifiedTextureColours = shortArrayOf(14000, 15000, 16000),
            recolourPalette = byteArrayOf(0, 1, 2, 3, 4),
            mirrored = true,
            castsShadow = false,
            modelSizeX = 64,
            modelSizeZ = 82,
            modelSizeY = 32,
            blockFlag = 1,
            offsetX = 10,
            offsetZ = 32,
            offsetY = 100,
            blocksLand = true,
            ignoreOnRoute = true,
            supportItems = 0,
            varbit = 10,
            varp = 1,
            transformIds = intArrayOf(100, 10, 1),
            transforms = emptyArray(),
            anInt3015 = 0,
            anInt3012 = 1,
            anInt2989 = 1,
            anInt2971 = 1,
            anIntArray3036 = intArrayOf(1, 2, 3),
            anInt3023 = 1,
            hideMinimap = true,
            aBoolean2972 = false,
            animateImmediately = false,
            isMembers = true,
            aBoolean3056 = true,
            aBoolean2998 = true,
            anInt2987 = 0,
            anInt3008 = 0,
            anInt3038 = 0,
            anInt3013 = 0,
            anInt2958 = 1,
            mapscene = 1,
            culling = 0,
            anInt3024 = 0,
            invertMapScene = true,
            animations = intArrayOf(1, 1000, 10000),
            percents = intArrayOf(22938, 16384, 26213),
            mapDefinitionId = 0,
            anIntArray2981 = intArrayOf(1, 2, 3),
            aByte2974 = 0,
            aByte3045 = 0,
            aByte3052 = 0,
            aByte2960 = 0,
            anInt2964 = 0,
            anInt2963 = 0,
            anInt3018 = 0,
            anInt2983 = 0,
            aBoolean2961 = true,
            aBoolean2993 = true,
            anInt3032 = 1012,
            anInt2962 = 1,
            anInt3050 = 512,
            anInt3020 = 512,
            aBoolean2992 = true,
            anInt2975 = 10,
            params = hashMapOf(1L to "string", 2L to 100000)
        )

        val encoder = ObjectEncoder()

        val writer = BufferWriter(1024)
        with(encoder) {
            writer.encode(definition)
        }

        val data = writer.array()
        val file = File("object-definition.dat")
        file.writeBytes(data)
        val stream = file.inputStream()
        val expected = stream.readAllBytes()
        assertArrayEquals(expected, data)
    }

}