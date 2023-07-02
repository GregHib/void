package world.gregs.voidps.tools

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import java.io.File

object ObjectDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache = CacheDelegate("./data/cache")

        val decoder = ObjectDecoder(false, true).loadCache(cache)
        val count = decoder.lastIndex
        println(count)
        var start = System.currentTimeMillis()


//        dump(cache, Indices.OBJECTS) { archive, file -> (archive shl 8) + file }
//        dump(cache, Indices.INTERFACES)
//        dump(cache, Indices.ANIMATIONS) { archive, file -> (archive shl 7) + file }
//        dump(cache, Indices.ENUMS) { archive, file -> (archive shl 8) + file }
//        dump(cache, Indices.GRAPHICS) { archive, file -> (archive shl 8) + file }
//        dump(cache, Indices.ITEMS) { archive, file -> (archive shl 8) + file }
//        dump(cache, Indices.NPCS) { archive, file -> (archive shl 7) + file}
//        dump(cache, Indices.QUICK_CHAT_MESSAGES)
//        dump(cache, Indices.QUICK_CHAT_MENUS)

//        loadAll2(cache, defs) { archive, file -> (archive shl 8) + file }
//        loadAll(defs)
        println("Loaded in ${System.currentTimeMillis() - start}ms")

    }

    fun dump(cache: Cache, index: Int, getId: (archive: Int, file: Int) -> Int = { a, _ -> a }) {
        val writer = BufferWriter(10_000_000)
        for (archiveId in cache.getArchives(index)) {
            val files = cache.getArchiveData(index, archiveId) ?: continue
            for ((fileId, file) in files) {
                if (file == null) {
                    continue
                }
                val id = getId(archiveId, fileId)
                writer.writeInt(id)
                writer.writeBytes(file)
            }
        }
        File("index${index}.dat").writeBytes(writer.toArray())
    }

    fun loadAll2(cache: Cache, definitions: Array<ObjectDefinition>, getId: (archive: Int, file: Int) -> Int) {
        for (archiveId in cache.getArchives(Indices.OBJECTS)) {
            val files = cache.getArchiveData(Indices.OBJECTS, archiveId) ?: continue
            for ((fileId, file) in files) {
                if (file == null) {
                    continue
                }
                val id = getId(archiveId, fileId)
                val definition = definitions[id]
                val buffer = BufferReader(file)
                while (true) {
                    val opcode = buffer.readUnsignedByte()
                    if (opcode == 0) {
                        break
                    }
                    definition.read(opcode, buffer)
                }
            }
        }
    }

    fun loadAll(definitions: Array<ObjectDefinition>) {
        val temp = File("temp.dat")
        val buffer = BufferReader(temp.readBytes())
        while (buffer.position() < buffer.length) {
            val id = buffer.readInt()
            val definition = definitions[id]
            while (true) {
                val opcode = buffer.readUnsignedByte()
                if (opcode == 0) {
                    break
                }
                definition.read(opcode, buffer)
            }
//            definition.changeValues()
        }
    }

    fun ObjectDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1, 5 -> {
                if (opcode == 5 && false) {
                    skip(buffer)
                }
                val length = buffer.readUnsignedByte()
                modelTypes = ByteArray(length)
                this.modelIds = Array(length) { count ->
                    modelTypes!![count] = buffer.readByte().toByte()
                    val size = buffer.readUnsignedByte()
                    val models = IntArray(size)
                    for (index in 0 until size) {
                        models[index] = buffer.readUnsignedShort()
                    }
                    models
                }
                if (opcode == 5 && !false) {
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
                val length = buffer.readShort()
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
            77, 92 -> readTransforms(buffer, opcode == 92)
            78 -> {
                anInt3015 = buffer.readShort()
                anInt3012 = buffer.readUnsignedByte()
            }
            79 -> {
                anInt2989 = buffer.readShort()
                anInt2971 = buffer.readShort()
                anInt3012 = buffer.readUnsignedByte()
                val length = buffer.readUnsignedByte()
                anIntArray3036 = IntArray(length)
                for (count in 0 until length) {
                    anIntArray3036!![count] = buffer.readShort()
                }
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
                    animations!![count] = buffer.readShort()
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
                if (!true) {
                    options!![opcode - 150] = null
                }
            }
            160 -> {
                val length = buffer.readUnsignedByte()
                anIntArray2981 = IntArray(length)
                for (count in 0 until length) {
                    anIntArray2981!![count] = buffer.readShort()
                }
            }
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

    private fun skip(buffer: Reader) {
        val length = buffer.readUnsignedByte()
        for (i in 0 until length) {
            buffer.skip(1)
            val amount = buffer.readUnsignedByte()
            buffer.skip(amount * 2)
        }
    }

    fun Array<ObjectDefinition>.findMatchingName(name: String) {
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.name.contains(name, true)) {
                println("Found $i ${def.options?.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }

    fun Array<ObjectDefinition>.findMatchingSize(width: Int, height: Int) {
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.sizeX == width && def.sizeY == height) {
                println("Found $i ${def.options?.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }

    fun Array<ObjectDefinition>.findMatchingModels(id: Int) {
        val original = getOrNull(id)!!
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.modelIds!!.contentDeepEquals(original.modelIds!!) && original.modifiedColours != null && def.modifiedColours.contentEquals(original.modifiedColours!!)) {
                println("Found $i ${def.options?.get(0)}")
            }
        }
    }

    fun Array<ObjectDefinition>.findTransforms(id: Int) {
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            if (def.transformIds?.contains(id) == true) {
                println("Found $i ${def.transformIds?.contentToString()}")
            }
        }
    }
}