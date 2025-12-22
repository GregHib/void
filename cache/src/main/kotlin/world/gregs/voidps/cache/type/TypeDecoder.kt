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
abstract class TypeDecoder<T : Type>(val size: Int, val opcodeSize: Int = 256) {

    init {
        check(opcodeSize <= 256) { "Field size cannot exceed 256: $opcodeSize" }
    }

    /**
     * The fields that are used in this type.
     */
    open val active: Set<Field> = emptySet()

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

    fun bool(key: String, default: Boolean, opcode: Int) = register(opcode, key, BooleanField(size, default))

    fun bool(key: String, default: Boolean, literal: Boolean, opcode: Int) = register(opcode, key, BooleanField(size, default, LiteralCodec(literal, BooleanCodec)))

    /** Byte - Key */
    fun byte(key: String, default: Byte) = registerKey(key, ByteField(size, default))

    /** Byte - Key + Opcode */
    fun byte(key: String, default: Byte, opcode: Int) = register(opcode, key, ByteField(size, default))

    /** Byte Literal - Key + Opcode */
    fun byte(key: String, default: Byte, literal: Byte, opcode: Int) = register(opcode, key, ByteField(size, default, LiteralCodec(literal, ByteCodec)))

    /** Unsigned Byte - Key */
    fun ubyte(key: String, default: Int) = registerKey(key, UByteField(size, default))

    /** Unsigned Byte - Key + Opcode */
    fun ubyte(key: String, default: Int, opcode: Int) = register(opcode, key, UByteField(size, default))

    fun byteArray(key: String, opcode: Int) = register(opcode, key, nullValue(size, NullByteArrayCodec))

    /** Short - Key */
    fun short(key: String, default: Short) = registerKey(key, ShortField(size, default))

    /** Short - Key + Opcode */
    fun short(key: String, default: Short, opcode: Int) = register(opcode, key, ShortField(size, default))

    /** Short Literal - Key + Opcode */
    fun short(key: String, default: Short, literal: Short, opcode: Int) = register(opcode, key, ShortField(size, default, LiteralCodec(literal, ShortCodec)))

    /** Unsigned Short - Key */
    fun ushort(key: String, default: Int) = registerKey(key, UShortField(size, default))

    /** Unsigned Short - Key + Opcode */
    fun ushort(key: String, default: Int, opcode: Int) = register(opcode, key, UShortField(size, default))

    fun shortArray(key: String, opcode: Int) = register(opcode, key, nullValue(size, NullShortArrayCodec))

    /** Int - Key */
    fun int(key: String, default: Int) = registerKey(key, IntField(size, default, IntCodec))

    /** Int - Key + Opcode */
    fun int(key: String, default: Int, opcode: Int) = register(opcode, key, IntField(size, default, IntCodec))

    /** Int Literal - Key + Opcode */
    fun int(key: String, default: Int, literal: Int, opcode: Int) = register(opcode, key, IntField(size, default, LiteralCodec(literal, IntCodec)))

    fun intArray(key: String, opcode: Int) = register(opcode, key, nullValue(size, NullIntArrayCodec))

    /** Nullable String - Key + Opcode */
    fun string(key: String, opcode: Int) = register(opcode, key, NullStringField(size))

    /** String - Key + Opcode */
    fun string(key: String, default: String, opcode: Int) = register(opcode, key, StringField(size, default))

    fun stringArray(key: String, default: Array<String>, opcode: Int) = register(opcode, key, value(size, default, StringArrayCodec))

    fun stringArray(key: String, opcode: Int) = register(opcode, key, nullValue(size, NullStringArrayCodec))

    fun shortArrays(first: String, second: String, opcode: Int): ShortArraysField {
        val field = ShortArraysField(size, first, second)
        registerKey(first, field)
        registerKey(second, field)
        registerField(opcode, field)
        return field
    }

    fun indexedStringArray(key: String, default: Array<String?>, opcodes: IntRange): IndexedStringArrayField {
        val field = IndexedStringArrayField(size, default, opcodes.first)
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

    fun stacks(idKey: String, amountKey: String, opcodes: IntRange) = register(IndexedNullIntArraysField(size, idKey, amountKey, opcodes.first), opcodes = opcodes)

    fun params(opcode: Int, block: ParameterBuilder.() -> Unit): ParameterField {
        val builder = ParameterBuilder()
        block.invoke(builder)
        val field = ParameterField(size, builder.paramIds)
        for (key in builder.paramIds.keys) {
            registerKey(key, field)
        }
        return registerField(opcode, field)
    }

    /**
     * Builder for defining parameter field mappings and transformations.
     *
     * Parameters are key-value pairs that can be stored in both config files and binary formats.
     * This builder allows you to:
     * - Map string keys to integer IDs for binary serialization
     * - Rename keys between internal and external representations
     * - Transform values between formats (e.g. Boolean to Int, Double to Int for binary storage)
     *
     * Example:
     * ```
     * params(opcode = 249) {
     *     add("ranged_strength", 643)
     *     convert("ranged_strength",
     *         decode = { (it as Int) / 10.0 },
     *         encode = { (it as Double * 10.0).toInt() }
     *     )
     * }
     * ```
     */
    class ParameterBuilder {
        val paramIds = mutableMapOf<String, Int>()
        val params = mutableMapOf<Int, String>()

        val transforms = mutableMapOf<String, Transform>() // FIXME
        val transformIds = mutableMapOf<Int, Transform>()

        val renames = mutableMapOf<String, String>()
        val originals = mutableMapOf<String, String>()
        var customIds = 10_000

        data class Transform(
            val configEncode: ((Any) -> Any)? = null,
            val configDecode: ((Any) -> Any)? = null,
            val binaryEncode: ((Any) -> Any)? = null,
            val binaryDecode: ((Any) -> Any)? = null,
        )

        fun add(vararg keys: String) {
            for (key in keys) {
                add(key)
            }
        }

        /**
         * Registers a parameter key with its binary ID.
         */
        fun add(key: String, value: Int = customIds++) {
            if (paramIds.containsKey(key)) {
                println("Duplicate parameter key: $key")
            }
            if (params.containsKey(value)) {
                println("Duplicate parameter value: $value")
            }
            paramIds[key] = value
            params[value] = key
        }

        /**
         * Renames a key between external (config/binary) and internal (runtime) representations.
         *
         * @param original The key name in config/binary files
         * @param modified The key name used internally in the Type
         */
        fun rename(original: String, modified: String) {
            renames[original] = modified
            originals[modified] = original
        }

        /**
         * Applies the same transformation for both config and binary formats.
         *
         * @param decode Transforms stored value to runtime format (on read)
         * @param encode Transforms runtime value to stored format (on write)
         */
        fun convert(key: String, decode: (Any) -> Any, encode: (Any) -> Any) {
            transforms[key] = Transform(encode, decode, encode, decode)
            paramIds[key]?.let { transformIds[it] = transforms[key]!! }
        }

        /**
         * Applies the same transformation to multiple keys.
         */
        fun convert(vararg keys: String, decode: (Any) -> Any, encode: (Any) -> Any) {
            for (key in keys) {
                convert(key, decode, encode)
            }
        }

        /**
         * Applies transformations only for config format.
         */
        fun convertConfig(key: String, decode: (Any) -> Any, encode: (Any) -> Any) {
            transforms[key] = Transform(encode, decode)
            paramIds[key]?.let { transformIds[it] = transforms[key]!! }
        }

        /**
         * Applies transformations only for binary format.
         */
        fun convertBinary(key: String, decode: (Any) -> Any, encode: (Any) -> Any) {
            transforms[key] = Transform(binaryDecode = decode, binaryEncode = encode)
            paramIds[key]?.let { transformIds[it] = transforms[key]!! }
        }

        fun convertBinary(vararg keys: String, decode: (Any) -> Any, encode: (Any) -> Any) {
            for (key in keys) {
                convertBinary(key, decode, encode)
            }
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
        for (i in 0 until size) {
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
    fun writePacked(writer: Writer, official: Boolean = false): Boolean {
        var written = false
        for (i in 0 until size) {
            written = written or writePacked(writer, i, official)
        }
        return written
    }

    /**
     * Write the decoders [index] values to a packed binary [writer].
     * @param official Whether to skip custom opcodes (250 and above)
     */
    fun writePacked(writer: Writer, index: Int, official: Boolean = false): Boolean {
        val start = writer.position()
        for ((opcode, field) in fields.withIndex()) {
            if (field == null || (opcode > 249 && official)) {
                continue
            }
            field.writePacked(writer, index, opcode)
        }
        if (writer.position() > start) {
            writer.writeByte(0)
            return true
        }
        return false
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