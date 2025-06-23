package world.gregs.voidps.tools.convert

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.OBJECTS
import world.gregs.voidps.cache.definition.Transforms
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull

open class ObjectDecoder718(
    val member: Boolean = true,
    val lowDetail: Boolean = false,
) : DefinitionDecoder<ObjectDefinitionFull>(OBJECTS) {

    override fun create(size: Int) = Array(size) { ObjectDefinitionFull(it) }

    override fun getFile(id: Int) = id and 0xff

    override fun getArchive(id: Int) = id ushr 8

    override fun ObjectDefinitionFull.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1, 5 -> {
                if (opcode == 5 && lowDetail) {
                    skip(buffer)
                }
                val length = buffer.readUnsignedByte()
                modelTypes = ByteArray(length)
                this.modelIds = Array(length) { count ->
                    modelTypes!![count] = buffer.readByte().toByte()
                    val size = buffer.readUnsignedByte()
                    IntArray(size) { buffer.readBigSmart() }
                }
                if (opcode == 5 && !lowDetail) {
                    skip(buffer)
                }
            }
            2 -> name = buffer.readString()
            14 -> sizeX = buffer.readUnsignedByte()
            15 -> sizeY = buffer.readUnsignedByte()
            17 -> {
                blocksSky = false
                solid = 0
                block = block and ObjectDefinition.PROJECTILE.inv()
            }
            18 -> {
                blocksSky = false
                block = block and ObjectDefinition.PROJECTILE.inv()
            }
            19 -> interactive = buffer.readUnsignedByte()
            21 -> contouredGround = 1
            22 -> delayShading = true
            23 -> culling = 1
            24 -> {
                val length = buffer.readBigSmart()
                if (length != 65535) {
                    animations = intArrayOf(length)
                }
            }
            27 -> solid = 1
            28 -> offsetMultiplier = buffer.readUnsignedByte() shl 2
            29 -> brightness = buffer.readByte()
            in 30..34 -> {
                if (options == null) {
                    options = arrayOf(null, null, null, null, null, "Examine")
                }
                options!![opcode - 30] = buffer.readString()
            }
            39 -> contrast = buffer.readByte() * 5
            40 -> readColours(buffer)
            41 -> readTextures(buffer)
            42 -> readColourPalette(buffer)
            62 -> mirrored = true
            64 -> castsShadow = false
            65 -> modelSizeX = buffer.readShort()
            66 -> modelSizeZ = buffer.readShort()
            67 -> modelSizeY = buffer.readShort()
            69 -> blockFlag = buffer.readUnsignedByte()
            70 -> offsetX = buffer.readUnsignedShort() shl 2
            71 -> offsetZ = buffer.readUnsignedShort() shl 2
            72 -> offsetY = buffer.readUnsignedShort() shl 2
            73 -> blocksLand = true
            74 -> {
                ignoreOnRoute = true
                block = block and ObjectDefinition.ROUTE.inv()
            }
            75 -> supportItems = buffer.readUnsignedByte()
            77, 92 -> readTransforms718(buffer, opcode == 92)
            78 -> {
                anInt3015 = buffer.readShort()
                anInt3012 = buffer.readUnsignedByte()
            }
            79 -> {
                anInt2989 = buffer.readShort()
                anInt2971 = buffer.readShort()
                anInt3012 = buffer.readUnsignedByte()
                val length = buffer.readUnsignedByte()
                anIntArray3036 = IntArray(length) { buffer.readShort() }
            }
            81 -> {
                contouredGround = 2.toByte()
                anInt3023 = buffer.readUnsignedByte() * 256
            }
            82 -> hideMinimap = true
            88 -> aBoolean2972 = false
            89 -> animateImmediately = false
            91 -> isMembers = true
            93 -> {
                contouredGround = 3
                anInt3023 = buffer.readShort()
            }
            94 -> contouredGround = 4
            95 -> {
                contouredGround = 5
                anInt3023 = buffer.readUnsignedShort()
            }
            97 -> aBoolean3056 = true
            98 -> aBoolean2998 = true
            99 -> {
                anInt2987 = buffer.readUnsignedByte()
                anInt3008 = buffer.readShort()
            }
            100 -> {
                anInt3038 = buffer.readUnsignedByte()
                anInt3013 = buffer.readShort()
            }
            101 -> anInt2958 = buffer.readUnsignedByte()
            102 -> mapscene = buffer.readShort()
            103 -> culling = 0
            104 -> anInt3024 = buffer.readUnsignedByte()
            105 -> invertMapScene = true
            106 -> {
                val length = buffer.readUnsignedByte()
                var total = 0
                animations = IntArray(length)
                percents = IntArray(length)
                for (count in 0 until length) {
                    animations!![count] = buffer.readBigSmart()
                    if (animations!![count] == 65535) {
                        animations!![count] = -1
                    }
                    percents!![count] = buffer.readUnsignedByte()
                    total += percents!![count]
                }
                for (count in 0 until length) {
                    percents!![count] = 65535 * percents!![count] / total
                }
            }
            107 -> mapDefinitionId = buffer.readShort()
            in 150..154 -> {
                if (options == null) {
                    options = arrayOf(null, null, null, null, null, "Examine")
                }
                options!![opcode - 150] = buffer.readString()
                if (!member) {
                    options!![opcode - 150] = null
                }
            }
            160 -> anIntArray2981 = IntArray(buffer.readUnsignedByte()) { buffer.readShort() }
            162 -> {
                contouredGround = 3
                anInt3023 = buffer.readInt()
            }
            163 -> {
                aByte2974 = buffer.readByte().toByte()
                aByte3045 = buffer.readByte().toByte()
                aByte3052 = buffer.readByte().toByte()
                aByte2960 = buffer.readByte().toByte()
            }
            164 -> anInt2964 = buffer.readUnsignedShort()
            165 -> anInt2963 = buffer.readUnsignedShort()
            166 -> anInt3018 = buffer.readUnsignedShort()
            167 -> anInt2983 = buffer.readShort()
            168 -> aBoolean2961 = true
            169 -> aBoolean2993 = true
            170 -> anInt3032 = buffer.readSmart()
            171 -> anInt2962 = buffer.readSmart()
            173 -> {
                anInt3050 = buffer.readShort()
                anInt3020 = buffer.readShort()
            }
            177 -> aBoolean2992 = true
            178 -> anInt2975 = buffer.readUnsignedByte()
            249 -> readParameters(buffer)
        }
    }
    private fun Transforms.readTransforms718(buffer: Reader, isLast: Boolean) {
        varbit = buffer.readShort()
        if (varbit == 65535) {
            varbit = -1
        }
        varp = buffer.readShort()
        if (varp == 65535) {
            varp = -1
        }
        var last = -1
        if (isLast) {
            last = buffer.readBigSmart()
            if (last == 65535) {
                last = -1
            }
        }
        val length = buffer.readUnsignedByte()
        transforms = IntArray(length + 2)
        for (count in 0..length) {
            transforms!![count] = buffer.readBigSmart()
            if (transforms!![count] == 65535) {
                transforms!![count] = -1
            }
        }
        transforms!![length + 1] = last
    }

    companion object {
        private fun skip(buffer: Reader) {
            val length = buffer.readUnsignedByte()
            for (i in 0 until length) {
                buffer.skip(1)
                val amount = buffer.readUnsignedByte()
                buffer.skip(amount * 2)
            }
        }
    }
}
