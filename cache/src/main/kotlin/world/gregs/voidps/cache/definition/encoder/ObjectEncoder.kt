package world.gregs.voidps.cache.definition.encoder

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.DefinitionEncoder
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import kotlin.math.roundToInt

class ObjectEncoder : DefinitionEncoder<ObjectDefinitionFull> {

    override fun Writer.encode(definition: ObjectDefinitionFull, members: ObjectDefinitionFull) {
        if (definition.id == -1) {
            return
        }

        val modelIds = definition.modelIds
        val modelTypes = definition.modelTypes
        if (modelIds != null && modelTypes != null) {
            writeByte(1)
            writeByte(modelIds.size)
            for ((index, models) in modelIds.withIndex()) {
                val type = definition.modelTypes!![index]
                writeByte(type.toInt())
                writeByte(models.size)
                for (model in models) {
                    writeShort(model)
                }
            }
        }

        val name = definition.name
        if (name != "null") {
            writeByte(2)
            writeString(name)
        }

        if (definition.sizeX != 1) {
            writeByte(14)
            writeByte(definition.sizeX)
        }

        if (definition.sizeY != 1) {
            writeByte(15)
            writeByte(definition.sizeY)
        }

        if (!definition.blocksSky) {
            if (definition.solid == 0) {
                writeByte(17)
            } else {
                writeByte(18)
            }
        }

        if (definition.interactive != -1) {
            writeByte(19)
            writeByte(definition.interactive)
        }

        if (definition.contouredGround == 1.toByte()) {
            writeByte(21)
        }

        if (definition.delayShading) {
            writeByte(22)
        }

        if (definition.culling == 1) {
            writeByte(23)
        }

        val animations = definition.animations
        val percents = definition.percents
        if (animations != null && percents == null) {
            writeByte(24)
            writeShort(animations[0])
        }

        if (definition.solid == 1) {
            writeByte(27)
        }

        if (definition.offsetMultiplier != 64) {
            writeByte(28)
            writeByte(definition.offsetMultiplier shr 2)
        }

        if (definition.brightness != 0) {
            writeByte(29)
            writeByte(definition.brightness)
        }

        val options = definition.options
        if (options != null) {
            for (index in 0 until 5) {
                val option = options[index] ?: continue
                writeByte(30 + index)
                writeString(option)
            }
        }

        if (definition.contrast != 0) {
            writeByte(39)
            writeByte(definition.contrast / 5)
        }

        definition.writeColoursTextures(this)

        definition.writeRecolourPalette(this)

        if (definition.mirrored) {
            writeByte(62)
        }

        if (!definition.castsShadow) {
            writeByte(64)
        }

        if (definition.modelSizeX != 128) {
            writeByte(65)
            writeShort(definition.modelSizeX)
        }

        if (definition.modelSizeZ != 128) {
            writeByte(66)
            writeShort(definition.modelSizeZ)
        }

        if (definition.modelSizeY != 128) {
            writeByte(67)
            writeShort(definition.modelSizeY)
        }

        if (definition.blockFlag != 0) {
            writeByte(69)
            writeByte(definition.blockFlag)
        }

        if (definition.offsetX != 0) {
            writeByte(70)
            writeShort(definition.offsetX shr 2)
        }

        if (definition.offsetZ != 0) {
            writeByte(71)
            writeShort(definition.offsetZ shr 2)
        }

        if (definition.offsetY != 0) {
            writeByte(72)
            writeShort(definition.offsetY shr 2)
        }

        if (definition.blocksLand) {
            writeByte(73)
        }

        if (definition.ignoreOnRoute) {
            writeByte(74)
        }

        if (definition.supportItems != -1) {
            writeByte(75)
            writeByte(definition.supportItems)
        }

        definition.writeTransforms(this, 77, 92)

        if (definition.anInt3015 != -1 || definition.anInt3012 != 0) {
            writeByte(78)
            writeShort(definition.anInt3015)
            writeByte(definition.anInt3012)
        }

        val anIntArray3036 = definition.anIntArray3036
        if (anIntArray3036 != null) {
            writeByte(79)
            writeShort(definition.anInt2989)
            writeShort(definition.anInt2971)
            writeByte(definition.anInt3012)
            writeByte(anIntArray3036.size)
            anIntArray3036.forEach {
                writeShort(it)
            }
        }

        if (definition.contouredGround == 2.toByte()) {
            writeByte(81)
            writeByte(definition.anInt3023 / 256)
        }

        if (definition.hideMinimap) {
            writeByte(82)
        }

        if (!definition.aBoolean2972) {
            writeByte(88)
        }

        if (!definition.animateImmediately) {
            writeByte(89)
        }

        if (definition.isMembers) {
            writeByte(91)
        }

        if (definition.contouredGround == 3.toByte() && definition.anInt3023 < Short.MAX_VALUE) {
            writeByte(93)
            writeShort(definition.anInt3023)
        }

        if (definition.contouredGround == 4.toByte()) {
            writeByte(94)
        }

        if (definition.contouredGround == 5.toByte()) {
            writeByte(95)
            writeShort(definition.anInt3023)
        }

        if (definition.aBoolean3056) {
            writeByte(97)
        }

        if (definition.aBoolean2998) {
            writeByte(98)
        }

        if (definition.anInt2987 != -1 || definition.anInt3008 != -1) {
            writeByte(99)
            writeByte(definition.anInt2987)
            writeShort(definition.anInt3008)
        }

        if (definition.anInt3038 != -1 || definition.anInt3013 != -1) {
            writeByte(100)
            writeByte(definition.anInt3038)
            writeShort(definition.anInt3013)
        }

        if (definition.anInt2958 != 0) {
            writeByte(101)
            writeByte(definition.anInt2958)
        }

        if (definition.mapscene != -1) {
            writeByte(102)
            writeShort(definition.mapscene)
        }

        if (definition.culling == 0) {
            writeByte(103)
        }

        if (definition.anInt3024 != 255) {
            writeByte(104)
            writeByte(definition.anInt3024)
        }

        if (definition.invertMapScene) {
            writeByte(105)
        }

        if (percents != null && animations != null) {
            writeByte(106)
            writeByte(animations.size)
            val total = percents.sum().toDouble()
            for (i in animations.indices) {
                writeShort(animations[i])
                writeByte(((percents[i] / total) * 100).roundToInt())
            }
        }

        if (definition.mapDefinitionId != -1) {
            writeByte(107)
            writeShort(definition.mapDefinitionId)
        }

        val membersOptions = members.options
        if (membersOptions != null) {
            for (index in 0 until 5) {
                val option = options?.get(index)
                if (option != null) {
                    continue
                }
                val membersOption = membersOptions[index] ?: continue
                writeByte(150 + index)
                writeString(membersOption)
            }
        }

        val anIntArray2981 = definition.anIntArray2981
        if (anIntArray2981 != null) {
            writeByte(160)
            writeByte(anIntArray2981.size)
            anIntArray2981.forEach {
                writeShort(it)
            }
        }

        if (definition.contouredGround == 3.toByte() && definition.anInt3023 >= Short.MAX_VALUE) {
            writeByte(162)
            writeInt(definition.anInt3023)
        }

        if (definition.aByte2974 != 0.toByte() || definition.aByte3045 != 0.toByte() || definition.aByte3052 != 0.toByte() || definition.aByte2960 != 0.toByte()) {
            writeByte(163)
            writeByte(definition.aByte2974.toInt())
            writeByte(definition.aByte3045.toInt())
            writeByte(definition.aByte3052.toInt())
            writeByte(definition.aByte2960.toInt())
        }

        if (definition.anInt2964 != 0) {
            writeByte(164)
            writeShort(definition.anInt2964)
        }

        if (definition.anInt2963 != 0) {
            writeByte(165)
            writeShort(definition.anInt2963)
        }

        if (definition.anInt3018 != 0) {
            writeByte(166)
            writeShort(definition.anInt3018)
        }

        if (definition.anInt2983 != 0) {
            writeByte(167)
            writeShort(definition.anInt2983)
        }

        if (definition.aBoolean2961) {
            writeByte(168)
        }

        if (definition.aBoolean2993) {
            writeByte(169)
        }

        if (definition.anInt3032 != 960) {
            writeByte(170)
            writeSmart(definition.anInt3032)
        }

        if (definition.anInt2962 != 0) {
            writeByte(171)
            writeSmart(definition.anInt2962)
        }

        if (definition.anInt3050 != 256 || definition.anInt3020 != 256) {
            writeByte(173)
            writeShort(definition.anInt3050)
            writeShort(definition.anInt3020)
        }

        if (definition.aBoolean2992) {
            writeByte(177)
        }

        if (definition.anInt2975 != 0) {
            writeByte(178)
            writeByte(definition.anInt2975)
        }

        definition.writeParameters(this)
        writeByte(0)
    }
}
