package world.gregs.voidps.tools

import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.MemoryCache
import world.gregs.voidps.cache.definition.data.ItemDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ItemDecoderFull
import world.gregs.voidps.cache.type.decode.ItemTypeDecoder
import world.gregs.voidps.cache.type.load.ItemLoader
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.type.ItemTypes
import java.io.File
import java.nio.ByteBuffer

object ItemDefinitions {

    abstract class Schema(val size: Int) {
        val fields = arrayOfNulls<ShortField>(256)

        fun short(opcode: Int, default: Short = 0) {
            fields[opcode] = ShortField(size, default)
        }
    }

    class SchemaImp(size: Int) : Schema(size) {
        val test = short(-1)
    }

    class ShortField(size: Int, default: Short) {
        val array: ShortArray = ShortArray(size) { default }

        fun read(index: Int, reader: ArrayReader) {
            array[index] = reader.readShort().toShort()
        }

        fun readRaw() {

        }

        fun writeRaw() {

        }
    }

    class DataClass(size: Int) {
        val modelId = ShortArray(size) { ItemDefinitionFull.EMPTY.modelId.toShort() }
        val name = Array(size) { ItemDefinitionFull.EMPTY.name }
        val spriteScale = ShortArray(size) { ItemDefinitionFull.EMPTY.spriteScale.toShort() }
        val spritePitch = ShortArray(size) { ItemDefinitionFull.EMPTY.spritePitch.toShort() }
        val spriteCameraRoll = ShortArray(size) { ItemDefinitionFull.EMPTY.spriteCameraRoll.toShort() }
        val spriteTranslateX = ShortArray(size) { ItemDefinitionFull.EMPTY.spriteTranslateX.toShort() }
        val spriteTranslateY = ShortArray(size) { ItemDefinitionFull.EMPTY.spriteTranslateY.toShort() }
        val stackable = ByteArray(size) { ItemDefinitionFull.EMPTY.stackable.toByte() }
        val cost = IntArray(size) { ItemDefinitionFull.EMPTY.cost }
        val members = ByteArray(size) { 0 }//ItemDefinitionFull.EMPTY.members }
        val multiStackSize = ShortArray(size) { ItemDefinitionFull.EMPTY.multiStackSize.toShort() }
        val primaryMaleModel = ShortArray(size) { ItemDefinitionFull.EMPTY.primaryMaleModel.toShort() }
        val secondaryMaleModel = ShortArray(size) { ItemDefinitionFull.EMPTY.secondaryMaleModel.toShort() }
        val primaryFemaleModel = ShortArray(size) { ItemDefinitionFull.EMPTY.primaryFemaleModel.toShort() }
        val secondaryFemaleModel = ShortArray(size) { ItemDefinitionFull.EMPTY.secondaryFemaleModel.toShort() }
        val floorOptions = Array(size) { ItemDefinitionFull.EMPTY.floorOptions.clone() }
        val options = Array(size) { ItemDefinitionFull.EMPTY.options.clone() }
        val originalColours = Array(size) { ItemDefinitionFull.EMPTY.originalColours }
        val modifiedColours = Array(size) { ItemDefinitionFull.EMPTY.modifiedColours }
        val originalTextureColours = Array(size) { ItemDefinitionFull.EMPTY.originalTextureColours }
        val modifiedTextureColours = Array(size) { ItemDefinitionFull.EMPTY.modifiedTextureColours }
        val recolourPalette = Array(size) { ItemDefinitionFull.EMPTY.recolourPalette }
        val exchangeable = ByteArray(size) { 0 }//ItemDefinitionFull.EMPTY.exchangeable }
        val tertiaryMaleModel = ShortArray(size) { ItemDefinitionFull.EMPTY.tertiaryMaleModel.toShort() }
        val tertiaryFemaleModel = ShortArray(size) { ItemDefinitionFull.EMPTY.tertiaryFemaleModel.toShort() }
        val primaryMaleDialogueHead = ShortArray(size) { ItemDefinitionFull.EMPTY.primaryMaleDialogueHead.toShort() }
        val primaryFemaleDialogueHead = ShortArray(size) { ItemDefinitionFull.EMPTY.primaryFemaleDialogueHead.toShort() }
        val secondaryMaleDialogueHead = ShortArray(size) { ItemDefinitionFull.EMPTY.secondaryMaleDialogueHead.toShort() }
        val secondaryFemaleDialogueHead = ShortArray(size) { ItemDefinitionFull.EMPTY.secondaryFemaleDialogueHead.toShort() }
        val spriteCameraYaw = ShortArray(size) { ItemDefinitionFull.EMPTY.spriteCameraYaw.toShort() }
        val dummyItem = ByteArray(size) { ItemDefinitionFull.EMPTY.dummyItem.toByte() }
        val noteId = ShortArray(size) { ItemDefinitionFull.EMPTY.noteId.toShort() }
        val notedTemplateId = ShortArray(size) { ItemDefinitionFull.EMPTY.notedTemplateId.toShort() }
        val stackIds = Array<ShortArray?>(size) { null }
        val stackAmounts = Array<ShortArray?>(size) { null }
        val floorScaleX = ShortArray(size) { ItemDefinitionFull.EMPTY.floorScaleX.toShort() }
        val floorScaleY = ShortArray(size) { ItemDefinitionFull.EMPTY.floorScaleY.toShort() }
        val floorScaleZ = ShortArray(size) { ItemDefinitionFull.EMPTY.floorScaleZ.toShort() }
        val ambience = ByteArray(size) { ItemDefinitionFull.EMPTY.ambience.toByte() }
        val diffusion = ByteArray(size) { ItemDefinitionFull.EMPTY.diffusion.toByte() }
        val team = ByteArray(size) { ItemDefinitionFull.EMPTY.team.toByte() }
        val lendId = ShortArray(size) { ItemDefinitionFull.EMPTY.lendId.toShort() }
        val lendTemplateId = ShortArray(size) { ItemDefinitionFull.EMPTY.lendTemplateId.toShort() }
        val maleWieldX = ByteArray(size) { ItemDefinitionFull.EMPTY.maleWieldX.toByte() }
        val maleWieldY = ByteArray(size) { ItemDefinitionFull.EMPTY.maleWieldY.toByte() }
        val maleWieldZ = ByteArray(size) { ItemDefinitionFull.EMPTY.maleWieldZ.toByte() }
        val femaleWieldX = ByteArray(size) { ItemDefinitionFull.EMPTY.femaleWieldX.toByte() }
        val femaleWieldY = ByteArray(size) { ItemDefinitionFull.EMPTY.femaleWieldY.toByte() }
        val femaleWieldZ = ByteArray(size) { ItemDefinitionFull.EMPTY.femaleWieldZ.toByte() }
        val primaryCursorOpcode = ByteArray(size) { ItemDefinitionFull.EMPTY.primaryCursorOpcode.toByte() }
        val primaryCursor = ShortArray(size) { ItemDefinitionFull.EMPTY.primaryCursor.toShort() }
        val secondaryCursorOpcode = ByteArray(size) { ItemDefinitionFull.EMPTY.secondaryCursorOpcode.toByte() }
        val secondaryCursor = ShortArray(size) { ItemDefinitionFull.EMPTY.secondaryCursor.toShort() }
        val primaryInterfaceCursorOpcode = ByteArray(size) { ItemDefinitionFull.EMPTY.primaryInterfaceCursorOpcode.toByte() }
        val primaryInterfaceCursor = ShortArray(size) { ItemDefinitionFull.EMPTY.primaryInterfaceCursor.toShort() }
        val secondaryInterfaceCursorOpcode = ByteArray(size) { ItemDefinitionFull.EMPTY.secondaryInterfaceCursorOpcode.toByte() }
        val secondaryInterfaceCursor = ShortArray(size) { ItemDefinitionFull.EMPTY.secondaryInterfaceCursor.toShort() }
        val pickSizeShift = ByteArray(size) { ItemDefinitionFull.EMPTY.pickSizeShift.toByte() }
        val singleNoteId = ShortArray(size) { ItemDefinitionFull.EMPTY.singleNoteId.toShort() }
        val singleNoteTemplateId = ShortArray(size) { ItemDefinitionFull.EMPTY.singleNoteTemplateId.toShort() }
        val params = Array(size) { ItemDefinitionFull.EMPTY.params }
    }


    fun write(array: Array<ItemDefinitionFull>, writer: ArrayWriter, default: Array<String?>, block: (ItemDefinitionFull) -> Array<String?>) {
        val keys = array.map { block(it).toList() }.flatten().distinct().sortedBy { it }
        writer.writeShort(keys.size)
        for (key in keys) {
            if (key == null) continue
            writer.writeString(key)
        }
        val map = keys.mapIndexed { it, s -> s to it }.toMap()
        for (i in array) {
            val options = block(i)
            for (j in options.indices) {
                val option = options[j]
                if (option == null || option == default[j]) {
                    continue
                }
                writer.writeByte(j)
                writer.writeShort(map[option]!!)
            }
            writer.writeByte(options.size + 1)
        }
    }

    fun write(array: Array<ItemDefinitionFull>, write: (ItemDefinitionFull) -> Unit) {
        for (i in array.indices) {
            val def = array[i]
            write(def)
        }
    }

    fun <T : Any?> read(reader: ArrayReader, array: Array<T>, read: () -> T) {
        for (i in 0 until array.size) {
            array[i] = read()
        }
    }

    fun read(reader: ArrayReader, array: Array<Array<String?>>) {
        val readKeys = Array(reader.readShort()) { if (it == 0) null else reader.readString() }
        for (i in 0 until array.size) {
            val options = array[i]
            while (true) {
                val index = reader.readUnsignedByte()
                if (index > options.size) {
                    break
                }
                val id = reader.readUnsignedShort()
                options[index] = readKeys[id]
            }
        }
    }

    fun read(reader: ArrayReader, array: IntArray) {
//        reader.buffer
//            .slice(reader.buffer.position(), array.size * 4)
//            .asIntBuffer()
//            .get(array)
        ByteBuffer.wrap(reader.array, reader.position(), array.size * 4)
            .asIntBuffer()
            .get(array)
        reader.position(reader.position() + (array.size * 4))
    }

    fun read(reader: ArrayReader, array: ShortArray) {
//        println("Read ${array.size}")
//        var start = reader.buffer.position()
//        reader.buffer
//            .slice(reader.buffer.position(), array.size * 2)
//            .asShortBuffer()
//            .get(array)
        ByteBuffer.wrap(reader.array, reader.position(), array.size * 2)
            .asShortBuffer()
            .get(array)
        reader.position(reader.position() + (array.size * 2))
//        println("End ${reader.buffer.position() - start}")
    }

    fun read(reader: ArrayReader, array: ByteArray) {
//        reader.buffer
//            .slice()
//            .get(array)
        System.arraycopy(reader.array, reader.position, array, 0, array.size)
        reader.position(reader.position() + array.size)
    }

    fun test(array: Array<ItemDefinitionFull>) {
        val size = array.size
        val s = System.currentTimeMillis()
        val data = DataClass(size)
        println("Allocation took ${System.currentTimeMillis() - s}ms")
        val writer = ArrayWriter(5_000_000)

        write(array) { writer.writeByte(it.stackable) }
        write(array) { writer.writeByte(it.members) }
        write(array) { writer.writeByte(it.exchangeable) }
        write(array) { writer.writeByte(it.dummyItem) }
        write(array) { writer.writeByte(it.ambience) }
        write(array) { writer.writeByte(it.diffusion) }
        write(array) { writer.writeByte(it.team) }
        write(array) { writer.writeByte(it.maleWieldX) }
        write(array) { writer.writeByte(it.maleWieldY) }
        write(array) { writer.writeByte(it.maleWieldZ) }
        write(array) { writer.writeByte(it.femaleWieldX) }
        write(array) { writer.writeByte(it.femaleWieldY) }
        write(array) { writer.writeByte(it.femaleWieldZ) }
        write(array) { writer.writeByte(it.primaryCursorOpcode) }
        write(array) { writer.writeByte(it.secondaryCursorOpcode) }
        write(array) { writer.writeByte(it.primaryInterfaceCursorOpcode) }
        write(array) { writer.writeByte(it.secondaryInterfaceCursorOpcode) }
        write(array) { writer.writeByte(it.pickSizeShift) }

        write(array) { writer.writeShort(it.modelId) }
        write(array) { writer.writeShort(it.spriteScale) }
        write(array) { writer.writeShort(it.spritePitch) }
        write(array) { writer.writeShort(it.spriteCameraRoll) }
        write(array) { writer.writeShort(it.spriteTranslateX) }
        write(array) { writer.writeShort(it.spriteTranslateY) }
        write(array) { writer.writeShort(it.multiStackSize) }
        write(array) { writer.writeShort(it.primaryMaleModel) }
        write(array) { writer.writeShort(it.secondaryMaleModel) }
        write(array) { writer.writeShort(it.primaryFemaleModel) }
        write(array) { writer.writeShort(it.secondaryFemaleModel) }
        write(array) { writer.writeShort(it.tertiaryMaleModel) }
        write(array) { writer.writeShort(it.tertiaryFemaleModel) }
        write(array) { writer.writeShort(it.primaryMaleDialogueHead) }
        write(array) { writer.writeShort(it.primaryFemaleDialogueHead) }
        write(array) { writer.writeShort(it.secondaryMaleDialogueHead) }
        write(array) { writer.writeShort(it.secondaryFemaleDialogueHead) }
        write(array) { writer.writeShort(it.spriteCameraYaw) }
        write(array) { writer.writeShort(it.noteId) }
        write(array) { writer.writeShort(it.notedTemplateId) }
        write(array) { writer.writeShort(it.floorScaleX) }
        write(array) { writer.writeShort(it.floorScaleY) }
        write(array) { writer.writeShort(it.floorScaleZ) }
        write(array) { writer.writeShort(it.lendId) }
        write(array) { writer.writeShort(it.lendTemplateId) }
        write(array) { writer.writeShort(it.primaryCursor) }
        write(array) { writer.writeShort(it.secondaryCursor) }
        write(array) { writer.writeShort(it.primaryInterfaceCursor) }
        write(array) { writer.writeShort(it.secondaryInterfaceCursor) }
        write(array) { writer.writeShort(it.singleNoteId) }
        write(array) { writer.writeShort(it.singleNoteTemplateId) }

        write(array) { writer.writeInt(it.cost) }

        write(array, { def -> def.originalColours }, { def -> def.modifiedColours }, writer)
        write(array, { def -> def.originalTextureColours }, { def -> def.modifiedTextureColours }, writer)
        write(array, { def -> def.stackIds?.map { it.toShort() }?.toShortArray() }, { def -> def.stackAmounts?.map { it.toShort() }?.toShortArray() }, writer)

        write(array, { def -> def.recolourPalette }, writer)


        write(array, writer, ItemDefinitionFull.EMPTY.floorOptions) { it.floorOptions }
        write(array, writer, ItemDefinitionFull.EMPTY.options) { it.options }
        write(array) { writer.writeString(it.name) }

        for (it in array) {
            val params = it.params
            if (params == null) {
                writer.writeByte(0)
                continue
            }
            writer.writeByte(params.size)
            for ((id, value) in params) {
                val type = if (value is String) 1 else 0
                writer.writeInt(type or (id shl 8))
                if (value is String) {
                    writer.writeString(value)
                } else if (value is Int) {
                    writer.writeInt(value)
                }
            }
        }
        val out = File("./temp.dat")
        out.writeBytes(writer.toArray())

        println("Size: ${writer.position()}")
        val start = System.currentTimeMillis()
        val reader = ArrayReader(out.readBytes())

        read(reader, data.stackable)
        read(reader, data.members)
        read(reader, data.exchangeable)
        read(reader, data.dummyItem)
        read(reader, data.ambience)
        read(reader, data.diffusion)
        read(reader, data.team)
        read(reader, data.maleWieldX)
        read(reader, data.maleWieldY)
        read(reader, data.maleWieldZ)
        read(reader, data.femaleWieldX)
        read(reader, data.femaleWieldY)
        read(reader, data.femaleWieldZ)
        read(reader, data.primaryCursorOpcode)
        read(reader, data.secondaryCursorOpcode)
        read(reader, data.primaryInterfaceCursorOpcode)
        read(reader, data.secondaryInterfaceCursorOpcode)
        read(reader, data.pickSizeShift)

        read(reader, data.modelId)
        read(reader, data.spriteScale)
        read(reader, data.spritePitch)
        read(reader, data.spriteCameraRoll)
        read(reader, data.spriteTranslateX)
        read(reader, data.spriteTranslateY)
        read(reader, data.multiStackSize)
        read(reader, data.primaryMaleModel)
        read(reader, data.secondaryMaleModel)
        read(reader, data.primaryFemaleModel)
        read(reader, data.secondaryFemaleModel)
        read(reader, data.tertiaryMaleModel)
        read(reader, data.tertiaryFemaleModel)
        read(reader, data.primaryMaleDialogueHead)
        read(reader, data.primaryFemaleDialogueHead)
        read(reader, data.secondaryMaleDialogueHead)
        read(reader, data.secondaryFemaleDialogueHead)
        read(reader, data.spriteCameraYaw)
        read(reader, data.noteId)
        read(reader, data.notedTemplateId)
        read(reader, data.floorScaleX)
        read(reader, data.floorScaleY)
        read(reader, data.floorScaleZ)
        read(reader, data.lendId)
        read(reader, data.lendTemplateId)
        read(reader, data.primaryCursor)
        read(reader, data.secondaryCursor)
        read(reader, data.primaryInterfaceCursor)
        read(reader, data.secondaryInterfaceCursor)
        read(reader, data.singleNoteId)
        read(reader, data.singleNoteTemplateId)

        read(reader, data.cost)

        read(data.originalColours, data.modifiedColours, reader)
        read(data.originalTextureColours, data.modifiedTextureColours, reader)
        read(data.stackIds, data.stackAmounts, reader)

        read(data.recolourPalette, reader)

        read(reader, data.floorOptions)
        read(reader, data.options)
        read(reader, data.name) { reader.readString() }

        for (i in 0 until size) {
            val size = reader.readByte()
            if (size == 0) {
                continue
            }
            val map = HashMap<Int, Any>(size)
            for (i in 0 until size) {
                val value = reader.readInt()
                val id = value ushr 8
                if (value and 0xff == 1) {
                    map[id] = reader.readString()
                } else {
                    map[id] = reader.readInt()
                }
            }
            data.params[i] = map
        }
        println("Took ${System.currentTimeMillis() - start}ms")
        println(data.params.map { it?.values ?: emptyList() }.flatten().filterIsInstance<String>().distinct().size)
        println(data.params.map { it?.values ?: emptyList() }.flatten().filterIsInstance<Int>().size)
        println(data.params.map { it?.values ?: emptyList() }.flatten().filterIsInstance<Int>().distinct().size)
        println("Read ${data.options.flatten().distinct().size}")
        println("Read ${data.floorOptions.flatten().distinct().size}")
    }

    private fun read(first: Array<ShortArray?>, second: Array<ShortArray?>, reader: ArrayReader) {
        for (i in first.indices) {
            val length = reader.readUnsignedByte()
            if (length == 0) {
                first[i] = null
                second[i] = null
                continue
            }
            val ids = ShortArray(length)
            read(reader, ids)
            first[i] = ids

            val amounts = ShortArray(length)
            read(reader, amounts)
            second[i] = amounts
        }
    }

    private fun read(array: Array<ByteArray?>, reader: ArrayReader) {
        for (i in array.indices) {
            val length = reader.readUnsignedByte()
            if (length == 0) {
                array[i] = null
                continue
            }
            val ids = ByteArray(length)
            read(reader, ids)
            array[i] = ids

            val amounts = ByteArray(length)
            read(reader, amounts)
        }
    }

    private fun write(array: Array<ItemDefinitionFull>, first: (ItemDefinitionFull) -> ShortArray?, second: (ItemDefinitionFull) -> ShortArray?, writer: ArrayWriter) {
        for (i in array.indices) {
            val data = first(array[i])
            if (data == null) {
                writer.writeByte(0)
                continue
            }
            writer.writeByte(data.size)
            for (id in data) {
                writer.writeShort(id.toInt())
            }
            for (amount in second(array[i])!!) {
                writer.writeShort(amount.toInt())
            }
        }
    }

    private fun write(array: Array<ItemDefinitionFull>, first: (ItemDefinitionFull) -> ByteArray?, writer: ArrayWriter) {
        for (i in array.indices) {
            val data = first(array[i])
            if (data == null) {
                writer.writeByte(0)
                continue
            }
            writer.writeByte(data.size)
            writer.writeBytes(data)
        }
    }

    fun directRoundTrip(actual: ItemTypeDecoder) {
        val writer = ArrayWriter(actual.directSize())
        actual.writeDirect(writer)

        val loader = ItemLoader()
        val expected = loader.decoder(actual.typeCount)
        val reader = ArrayReader(writer.toArray())
        expected.readDirect(reader)
        assert(expected == actual)
    }

    fun packedRoundTrip(original: ItemTypeDecoder) {
        val loader = ItemLoader()
        val writer = ArrayWriter(5_000_000)
        for (i in 0 until original.typeCount) {
            original.writePacked(writer, i)
        }
        val reader = ArrayReader(writer.toArray())
        val actual = loader.decoder(original.typeCount)
        for (i in 0 until original.typeCount) {
            actual.readPacked(reader, i)
        }
        assert(original == actual)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
//        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
//        val categories = CategoryDefinitions().load(files.find(Settings["definitions.categories"]))
//        val ammo = AmmoDefinitions().load(files.find(Settings["definitions.ammoGroups"]))
//        val parameters = ParameterDefinitions(categories, ammo).load(files.find(Settings["definitions.parameters"]))
        val modified: Long = if (false) 0 else System.currentTimeMillis()
        File(Settings["storage.data.modified"]).writeBytes(ArrayWriter(8).also { it.writeLong(modified) }.toArray())
        val memoryCache = MemoryCache.load(Settings["storage.cache.path"])

        val defs = ItemDecoderFull().load(memoryCache)
//        test(defs)

        val dec = ItemDecoderFull()
        val size = dec.size(cache = memoryCache)
//        val array = dec.create(size)
//        println(array.size)
        val files = configFiles()
//        val loader = ItemLoader()
//        val actual = loader.decoder(size)
//        val reader = ArrayReader()
//        val start = System.currentTimeMillis()
//        for (i in 0 until size) {
//            val data = loader.data(memoryCache, i) ?: continue
//            reader.set(data)
//            actual.readPacked(reader, i)
//        }
//        loader.applyConfigs(actual, files.list("items.toml"))
//        println("Loaded from cache in ${System.currentTimeMillis() - start}ms")
//        println("Configs ${System.currentTimeMillis() - start}ms")
//        directRoundTrip(actual)
//        types = loader.load(cache, paths, files.extensions.contains(extension), files.cacheUpdate)
//        val decoder = dec.load(memoryCache)
//        test(decoder)
//        val decoder = ItemDecoderFull().load(memoryCache)
        ItemTypes.load(memoryCache, files)
    }
}
