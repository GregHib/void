package world.gregs.voidps.cache.type

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeSection
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.*
import world.gregs.voidps.cache.type.field.codec.BooleanCodec
import world.gregs.voidps.cache.type.field.codec.ByteCodec
import world.gregs.voidps.cache.type.field.codec.IntCodec
import world.gregs.voidps.cache.type.field.codec.LiteralCodec
import world.gregs.voidps.cache.type.field.codec.NullByteArrayCodec
import world.gregs.voidps.cache.type.field.codec.NullIntArrayCodec
import world.gregs.voidps.cache.type.field.codec.NullShortArrayCodec
import world.gregs.voidps.cache.type.field.codec.NullStringArrayCodec
import world.gregs.voidps.cache.type.field.codec.ShortCodec
import world.gregs.voidps.cache.type.field.codec.StringArrayCodec
import world.gregs.voidps.cache.type.field.codec.UnsignedShortCodec
import world.gregs.voidps.cache.type.field.type.BooleanField
import world.gregs.voidps.cache.type.field.type.ByteField
import world.gregs.voidps.cache.type.field.type.PairField
import world.gregs.voidps.cache.type.field.type.TripleField
import world.gregs.voidps.cache.type.field.custom.IndexedNullIntArraysField
import world.gregs.voidps.cache.type.field.custom.IndexedStringArrayField
import world.gregs.voidps.cache.type.field.type.IntField
import world.gregs.voidps.cache.type.field.type.NullStringField
import world.gregs.voidps.cache.type.field.custom.ParameterField
import world.gregs.voidps.cache.type.field.custom.ShortArraysField
import world.gregs.voidps.cache.type.field.custom.TransformField
import world.gregs.voidps.cache.type.field.custom.TranslateField
import world.gregs.voidps.cache.type.field.type.QuadField
import world.gregs.voidps.cache.type.field.type.QuinField
import world.gregs.voidps.cache.type.field.type.ShortField
import world.gregs.voidps.cache.type.field.type.StringField
import world.gregs.voidps.cache.type.field.type.UByteField
import world.gregs.voidps.cache.type.field.type.UShortField

/**
 * Base class for defining the schema and serialization logic for a [Type].
 *
 * [fields] hold the data to be serialized between formats. Each field is [register]'ed with an opcode
 * for binary serialization and a key for [world.gregs.config.Config] serialization.
 *
 * Note:
 *  - [active] fields can be specified for further optimised caching.
 *  - [id] and [stringId] is required for config decoding (though [id] should be omitted from [active] fields).
 *
 * Usage example:
 * ```
 * data class ItemType(
 *     val name: String,
 *     val cost: Int
 * ) : Type
 *
 * class ItemTypeDecoder : TypeDecoder<ItemType>() {
 *     private val name = string("[section]", "default", opcode = 15)
 *     private val cost = int("cost", 0, opcode = 10)
 *
 *     override val active = setOf(name, cost)
 *     override fun create() = ItemType(
 *         name = name.value,
 *         cost = cost.value
 *     )
 *     override fun load(type: ItemType) {
 *         name.value = type.name
 *         cost.value = type.cost
 *     }
 * }
 * ```
 *
 * @param T The Type that this decoder creates
 */
abstract class TypeDecoder<T : Type>(val typeCount: Int, val opcodeSize: Int = 256) {

    init {
        check(opcodeSize <= 256) { "Field size cannot exceed 256: $opcodeSize" }
    }

    /**
     * The fields that are used in this type.
     */
    open val active: Set<Field> = emptySet()

    /**
     * Fields that are custom and shouldn't be written to cache.
     */
    open val custom: Set<Field> = setOf(id, stringId)

    abstract val id: AccessibleField<Int>
    abstract val stringId: AccessibleField<String>

    /**
     * Maps opcodes to their corresponding fields for binary serialization.
     */
    val fields: Array<Field?> = arrayOfNulls(opcodeSize)

    val keys: MutableMap<String, Field> = mutableMapOf()

    /**
     * Creates a new instance of the Type using the current field values.
     */
    abstract fun create(index: Int): T

    /**
     * Load a type back into the memory of this decoder.
     */
    abstract fun load(type: T)

    /**
     * Load all types into memory.
     */
    fun load(types: List<T>) {
        for (type in types) {
            load(type)
        }
    }

    open fun loaded(types: Array<T?>) {}

    /**
     * Validation error message set during field registration.
     * Done this way to avoid creating hard to debug build-time errors.
     */
    var error: String? = null

    /*
        =====================
        === Field Helpers ===
        =====================
        Methods to help create and registers common fields with their opcodes.
     */

    fun bool(key: String, default: Boolean, opcode: Int) = register(opcode, key, BooleanField(typeCount, default))

    fun bool(key: String, default: Boolean, literal: Boolean, opcode: Int) = register(opcode, key, BooleanField(typeCount, default, LiteralCodec(literal, BooleanCodec)))

    /** Byte - Key */
    fun byte(key: String, default: Byte) = registerKey(key, ByteField(typeCount, default))

    /** Byte - Key + Opcode */
    fun byte(key: String, default: Byte, opcode: Int) = register(opcode, key, ByteField(typeCount, default))

    /** Byte Literal - Key + Opcode */
    fun byte(key: String, default: Byte, literal: Byte, opcode: Int) = register(opcode, key, ByteField(typeCount, default, LiteralCodec(literal, ByteCodec)))

    /** Unsigned Byte - Key */
    fun ubyte(key: String, default: Int) = registerKey(key, UByteField(typeCount, default))

    /** Unsigned Byte - Key + Opcode */
    fun ubyte(key: String, default: Int, opcode: Int) = register(opcode, key, UByteField(typeCount, default))

    fun byteArray(key: String, opcode: Int) = register(opcode, key, nullValue(typeCount, NullByteArrayCodec))

    /** Short - Key */
    fun short(key: String, default: Short) = registerKey(key, ShortField(typeCount, default))

    /** Short - Key + Opcode */
    fun short(key: String, default: Short, opcode: Int) = register(opcode, key, ShortField(typeCount, default))

    /** Short Literal - Key + Opcode */
    fun short(key: String, default: Short, literal: Short, opcode: Int) = register(opcode, key, ShortField(typeCount, default, LiteralCodec(literal, ShortCodec)))

    /** Unsigned Short - Key */
    fun ushort(key: String, default: Int) = registerKey(key, UShortField(typeCount, default))

    /** Unsigned Short - Key + Opcode */
    fun ushort(key: String, default: Int, opcode: Int) = register(opcode, key, UShortField(typeCount, default))

    fun shortArray(key: String, opcode: Int) = register(opcode, key, nullValue(typeCount, NullShortArrayCodec))

    fun ushortArray(key: String, opcode: Int) = register(opcode, key, nullValue(typeCount, NullIntArrayCodec(UnsignedShortCodec)))

    /** Int - Key */
    fun int(key: String, default: Int) = registerKey(key, IntField(typeCount, default, IntCodec))

    /** Int - Key + Opcode */
    fun int(key: String, default: Int, opcode: Int) = register(opcode, key, IntField(typeCount, default, IntCodec))

    /** Int Literal - Key + Opcode */
    fun int(key: String, default: Int, literal: Int, opcode: Int) = register(opcode, key, IntField(typeCount, default, LiteralCodec(literal, IntCodec)))

    fun intArray(key: String, opcode: Int) = register(opcode, key, nullValue(typeCount, NullIntArrayCodec))

    /** Nullable String - Key + Opcode */
    fun string(key: String, opcode: Int) = register(opcode, key, NullStringField(typeCount))

    /** String - Key + Opcode */
    fun string(key: String, default: String, opcode: Int) = register(opcode, key, StringField(typeCount, default))

    fun stringArray(key: String, default: Array<String>, opcode: Int) = register(opcode, key, value(typeCount, default, StringArrayCodec))

    fun stringArray(key: String, opcode: Int) = register(opcode, key, nullValue(typeCount, NullStringArrayCodec))

    fun shortArrays(first: String, second: String, opcode: Int): ShortArraysField {
        val field = ShortArraysField(typeCount, first, second)
        registerKey(first, field)
        registerKey(second, field)
        registerField(opcode, field)
        return field
    }

    fun indexedStringArray(key: String, default: Array<String?>, opcodes: IntRange): IndexedStringArrayField {
        val field = IndexedStringArrayField(typeCount, default, opcodes.first)
        registerKey(key, field)
        for (opcode in opcodes) {
            registerField(opcode, field)
        }
        return field
    }

    inline fun <reified T : Any> value(size: Int, default: T, codec: FieldCodec<T>): ValueField<T> = ValueField(default, codec, create = { Array(size) { default } })

    inline fun <reified T : Any> nullValue(size: Int, codec: FieldCodec<T?>): NullValueField<T> = NullValueField(codec, create = { arrayOfNulls(size) })

    fun <A, B> pair(first: PrimitiveField<A>, second: PrimitiveField<B>, opcode: Int) = registerField(opcode, PairField(first, second))

    fun <A, B, C> triple(first: PrimitiveField<A>, second: PrimitiveField<B>, third: PrimitiveField<C>, opcode: Int) = registerField(opcode, TripleField(first, second, third))

    fun <A, B, C, D> quad(first: PrimitiveField<A>, second: PrimitiveField<B>, third: PrimitiveField<C>, fourth: PrimitiveField<D>, opcode: Int) = registerField(opcode, QuadField(first, second, third, fourth))

    fun <A, B, C, D, E> quin(first: PrimitiveField<A>, second: PrimitiveField<B>, third: PrimitiveField<C>, fourth: PrimitiveField<D>, fifth: PrimitiveField<E>, opcode: Int) = registerField(opcode, QuinField(first, second, third, fourth, fifth))

    fun transforms(firstOpcode: Int, lastOpcode: Int): TransformField {
        val field = TransformField(typeCount, lastOpcode)
        registerKey("transforms", field)
        registerKey("varbit", field)
        registerKey("varp", field)
        registerField(firstOpcode, field)
        return registerField(lastOpcode, field)
    }

    fun translate(key: String, modelIds: NullValueField<IntArray>, opcode: Int) = register(opcode, key, TranslateField(typeCount, modelIds))

    fun stacks(idKey: String, amountKey: String, opcodes: IntRange) = register(IndexedNullIntArraysField(typeCount, idKey, amountKey, opcodes.first), opcodes = opcodes)

    fun params(opcode: Int, block: ParameterBuilder.() -> Unit): ParameterField {
        val builder = ParameterBuilder()
        block.invoke(builder)
        val field = ParameterField(typeCount, builder.ids, builder.params)
        for (key in builder.ids.keys) {
            registerKey(key, field)
        }
        return registerField(opcode, field)
    }

    /**
     * Builder for defining parameter field mappings and transformations.
     *
     * Parameters are key-value pairs that can be stored in both config files and binary formats.
     * This builder allows you to map string keys to integer IDs for binary serialization.
     * When ids aren't provided, a custom id is generated and will be consistent presuming field initiation order isn't changed.
     *
     * Example:
     * ```
     * params(opcode = 249) {
     *     add("ranged_strength", 643)
     *     add("custom_param")
     * }
     * ```
     */
    class ParameterBuilder {
        val ids = mutableMapOf<String, Int>()
        val params = mutableMapOf<Int, String>()
        var customIds = 10_000

        fun add(vararg keys: String) {
            for (key in keys) {
                add(key)
            }
        }

        /**
         * Registers a parameter key with its binary ID.
         */
        fun add(key: String, value: Int = customIds++) {
            if (ids.containsKey(key)) {
                println("Duplicate parameter key: $key")
            }
            if (params.containsKey(value)) {
                println("Duplicate parameter id: $value")
            }
            ids[key] = value
            params[value] = key
        }
    }

    inline fun <reified F : Field> register(field: F, opcodes: IntRange): F {
        for (opcode in opcodes) {
            registerField(opcode, field)
        }
        return field
    }

    inline fun <reified F : Field> register(opcode: Int, key: String, field: F): F {
        registerKey(key, field)
        registerField(opcode, field)
        return field
    }

    fun <F : Field> registerField(opcode: Int, field: F): F {
        if (opcode > 0 && fields[opcode] != null) {
            error = "Duplicate opcodes: $opcode"
        }
        if (opcode == 0) {
            error = "Zero is not a valid opcode"
        }
        if (opcode > 256) {
            error = "Opcodes can't exceed 256: $opcode"
        }
        fields[opcode] = field
        return field
    }

    fun <F : Field> registerKey(key: String, field: F): F {
        if (keys.containsKey(key)) {
            error = "Duplicate field key: $key"
        }
        keys[key] = field
        return field
    }

    inline fun <reified F : Field> register(field: F, key: String, opcodes: IntRange): F {
        for (opcode in opcodes) {
            register(opcode, key, field)
        }
        return field
    }

    /**
     * Check post-compilation for errors or invalid fields.
     */
    fun check() {
        require(error == null) { error!! }
    }

    /**
     * Override [to] values in decoder with non-default values [from] [other].
     */
    fun override(other: TypeDecoder<T>, from: Int, to: Int = from) {
        for (i in fields.indices) {
            val field = fields[i] ?: continue
            field.override(other.fields[i]!!, from, to)
        }
    }

    /**
     * Read all types from a packed binary [reader].
     */
    fun readPacked(reader: Reader) {
        for (i in 0 until typeCount) {
            readPacked(reader, i)
        }
    }

    /**
     * Read a single type [index] from a packed binary [reader].
     */
    fun readPacked(reader: Reader, index: Int) {
        val start = reader.position()
        while (true) {
            val code = reader.readUnsignedByte()
            if (code == 0) {
                break
            }
            val field = fields[code]
            if (field == null) {
                reader.position(start)
                val list = replay(reader, index, code)
                throw IllegalArgumentException("Unknown opcode: $code. Is it register in the type decoder? Previous opcodes: $list")
            }
            field.readPacked(reader, index, code)
        }
    }

    /**
     * Replay a packet for debugging purposes.
     * Note: Decoding errors typically occur reading the last valid opcode before bytes started to be read erroneously.
     */
    private fun replay(reader: Reader, index: Int, code: Int): List<Int> {
        val list = mutableListOf<Int>()
        while (true) {
            val temp = reader.readUnsignedByte()
            if (temp == code || temp == 0) {
                break
            }
            list.add(temp)
            val field = fields[code] ?: break
            field.clear()
            field.readPacked(reader, index, temp)
        }
        return list
    }

    /**
     * Write all values to a packed binary [writer].
     * @param official Whether to skip custom opcodes (250 and above)
     */
    fun writePacked(writer: Writer, official: Boolean = false) {
        for (i in 0 until typeCount) {
            writePacked(writer, i, official)
        }
    }

    /**
     * Write the decoders [index] values to a packed binary [writer].
     * @param official Whether to skip custom opcodes (250 and above)
     */
    fun writePacked(writer: Writer, index: Int, official: Boolean = false) {
        val start = writer.position()
        for ((opcode, field) in fields.withIndex()) {
            if (field == null || (opcode > 249 && official)) {
                continue
            }
            field.writePacked(writer, index, opcode)
        }
        if (writer.position() > start) {
            writer.writeByte(0)
        }
    }

    /**
     * Read a single type [index] from a config [reader].
     * Note: this assumes [stringId] is the section header.
     */
    fun readConfig(reader: ConfigReader, index: Int) {
        stringId.set(index, reader.section())
        while (reader.nextPair()) {
            val key = reader.key()
            val field = keys[key] ?: throw IllegalArgumentException("Unknown field '$key'. Is it registered in the type decoder?")
            field.readConfig(reader, index, key)
        }
    }

    /**
     * Write the decoders [index] values to a config [writer].
     * Note: this assumes [stringId] is the section header.
     */
    fun writeConfig(writer: ConfigWriter, index: Int) {
        writer.writeSection(stringId.get(index))
        val written = mutableSetOf<String>()
        for ((key, field) in keys) {
            if (field == stringId) {
                continue
            }
            if (!written.add(key)) {
                continue
            }
            field.writeConfig(writer, index, key)
        }
    }

    /**
     * Read all data from [reader]
     */
    fun readDirect(reader: Reader) {
        var index = 0
        try {
            for (field in fields) {
                index++
                if (field == null) {
                    continue
                }
                field.readDirect(reader)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error reading field opcode: ${index - 1}")
        }
    }

    /**
     * Write all data to [writer]
     * Make sure [writer] capacity exceeds [directSize]
     */
    fun writeDirect(writer: Writer) {
        for (field in fields) {
            if (field == null) {
                continue
            }
            val start = writer.position()
            field.writeDirect(writer)
            val actual = writer.position() - start
            val calc = field.directSize()
            if (actual != calc) {
                throw IllegalStateException("Field ${fields.indexOf(field)} size mismatch: calculated $calc, actual $actual.")
            }
        }
    }

    /**
     * Calculate exact size of data to be written by [writeDirect]
     */
    fun directSize(): Int {
        var size = 0
        for (field in fields) {
            if (field == null) {
                continue
            }
            size += field.directSize()
        }
        return size
    }

    /**
     * Clear all field values to their default state.
     */
    fun clear() {
        for (field in fields) {
            field?.clear()
        }
    }

    /**
     * List of all the active opcodes
     */
    fun activeOpcodes(): ByteArray {
        val array = mutableListOf<Byte>()
        for (i in fields.indices) {
            val field = fields[i] ?: continue
            if (active.contains(field)) {
                array.add(i.toByte())
            }
        }
        return array.toByteArray()
    }

    /**
     * Clears all fields which aren't [active] allowing for smaller save sizes
     */
    fun clearInactive() {
        for (field in fields) {
            if (active.contains(field)) {
                continue
            }
            field?.clear()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypeDecoder<T>

        for (i in fields.indices) {
            val field = fields[i]
            val other = other.fields[i]
            if (field != other) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        for (field in fields) {
            result = 31 * result + field.hashCode()
        }
        return result
    }

    override fun toString(): String {
        return "TypeDecoder(id=$id, fields=${fields.toList()},keys=${keys.keys})"
    }

}