package world.gregs.voidps.cache.type

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeSection
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.*
import world.gregs.voidps.cache.type.field.type.IndexedStringArrayField
import world.gregs.voidps.cache.type.field.codec.*
import world.gregs.voidps.cache.type.field.type.ColourField
import world.gregs.voidps.cache.type.field.type.FieldPair
import world.gregs.voidps.cache.type.field.type.FieldTriple
import world.gregs.voidps.cache.type.field.type.ParameterField

/**
 * Base class for defining the schema and serialization logic for a [Type].
 *
 * Subclasses define the fields that make up a Type and provide the logic for
 * creating instances from those fields. Each field is registered with an opcode
 * for binary serialization and a key for TOML config serialization.
 *
 * Note: It's important that fields are registered in the same order as they appear in the data object.
 *
 * Example:
 * ```
 * data class ItemType(
 *     val name: String,
 *     val cost: Int
 * ) : Type
 *
 * class ItemTypeDecoder : TypeDecoder<ItemType>() {
 *     private val NAME = string("[section]", "default", opcode = 15)
 *     private val COST = int("cost", 0, opcode = 10)
 *
 *     override fun create() = ItemType(
 *         name = NAME.value,
 *         cost = COST.value
 *     )
 * }
 * ```
 *
 * @param T The Type that this decoder creates
 */
abstract class TypeDecoder<T : Type>(val size: Int) {
    /**
     * Maps opcodes to their corresponding fields for binary serialization.
     */
    val fields: Array<TypeField?> = arrayOfNulls(size)
    val extras = mutableListOf<TypeField>()

    /**
     * Creates a new instance of the Type using the current field values.
     */
    abstract fun create(): T

    abstract fun load(type: T)

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

    fun bool(key: String, default: Boolean, opcode: Int) = register(opcode, key, default, BooleanCodec)

    fun boolLiteral(key: String, default: Boolean, value: Boolean, opcode: Int) = register(opcode, key, default, LiteralCodec(value, BooleanCodec))

    fun byte(key: String, default: Int, opcode: Int) = register(opcode, key, default, ByteCodec)

    fun byteArray(key: String, opcode: Int) = register(opcode, key, null, NullByteArrayCodec)

    fun ubyte(key: String, default: Int, opcode: Int) = register(opcode, key, default, UnsignedByteCodec)

    fun short(key: String, default: Int, opcode: Int) = register(opcode, key, default, ShortCodec)

    fun ushort(key: String, default: Int, opcode: Int) = register(opcode, key, default, UnsignedShortCodec)

    fun shortArray(key: String, opcode: Int) = register(opcode, key, null, NullShortArrayCodec)

    fun int(key: String, default: Int, opcode: Int = -1) = register(opcode, key, default, IntCodec)

    fun intLiteral(key: String, default: Int, value: Int, opcode: Int) = register(opcode, key, default, LiteralCodec(value, IntCodec))

    fun intArray(key: String, opcode: Int) = register(opcode, key, null, NullIntArrayCodec)

    fun string(key: String, default: String, opcode: Int = -1) = register(opcode, key, default, StringCodec)

    fun string(key: String, opcode: Int): ValueField<String?> {
        return register(opcode, key, null, NullStringCodec)
    }

    fun indexedStringArray(key: String, default: Array<String?>, opcodes: IntRange): IndexedStringArrayField {
        val field = IndexedStringArrayField(key, default, opcodes.first)
        for (opcode in opcodes) {
            register(opcode, field)
        }
        return field
    }

    operator fun <T : Any> FieldCodec<T>.invoke(key: String, default: T) = ValueField(key, default, this)

    fun <A, B> pair(first: ValueField<A>, second: ValueField<B>, opcode: Int) = register(opcode, FieldPair(first, second))

    fun <A, B, C> triple(first: ValueField<A>, second: ValueField<B>, third: ValueField<C>, opcode: Int) = register(opcode, FieldTriple(first, second, third))

    fun colours(original: String, modified: String, opcode: Int) = register(opcode, ColourField(original, modified))

    fun params(opcode: Int, block: ParameterBuilder.() -> Unit): ParameterField {
        val builder = ParameterBuilder()
        block.invoke(builder)
        return register(opcode, ParameterField(builder.paramIds, builder.params, builder.transforms, builder.renames, builder.originals))
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

        val transforms = mutableMapOf<String, Transform>()

        val renames = mutableMapOf<String, String>()
        val originals = mutableMapOf<String, String>()

        data class Transform(
            val configEncode: ((Any) -> Any)? = null,
            val configDecode: ((Any) -> Any)? = null,
            val binaryEncode: ((Any) -> Any)? = null,
            val binaryDecode: ((Any) -> Any)? = null,
        )

        /**
         * Registers a parameter key with its binary ID.
         */
        fun add(key: String, value: Int) {
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
        }

        /**
         * Applies transformations only for binary format.
         */
        fun convertBinary(key: String, decode: (Any) -> Any, encode: (Any) -> Any) {
            transforms[key] = Transform(binaryDecode = decode, binaryEncode = encode)
        }
    }

    fun <T : Any?> register(opcode: Int, key: String, default: T, codec: FieldCodec<T>) = register(opcode, ValueField(key, default, codec))

    inline fun <reified F : TypeField> register(opcode: Int, field: F): F {
        if (opcode < 0) {
            extras.add(field)
            return field
        }
        if (opcode > 0 && fields[opcode] != null) {
            error = "Duplicate opcodes: $opcode"
        }
        if (opcode == 0) {
            error = "Zero is not a valid opcode"
        }
        fields[opcode] = field
        return field
    }

    inline fun <reified F : TypeField> register(field: F, opcodes: IntRange): F {
        for (opcode in opcodes) {
            register(opcode, field)
        }
        return field
    }

    /**
     *  Check post-compilation for errors or invalid fields.
     */
    fun check() {
        require(error == null) { error!! }
        val duplicateKeys = (fields + extras)
            .flatMap { it?.keys ?: emptyList() }
            .groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
        require(duplicateKeys.isEmpty()) {
            "Duplicate field names: ${duplicateKeys.keys}"
        }
    }

    /**
     * Reset all field values to their default state.
     */
    fun reset() {
        for (field in fields) {
            field?.reset()
        }
        for (field in extras) {
            field.reset()
        }
    }

    private val resetArray = IntArray(size)
    private var resetIndex = 0

    private fun resetFlags() {
        val resetArray = resetArray
        val fields = fields
        for (idx in 0 until resetIndex) {
            fields[resetArray[idx]]!!.reset()
        }
        resetIndex = 0
    }

    private fun flag(code: Int) {
        resetArray[resetIndex++] = code
    }

    /**
     * Read a single type from a binary file.
     */

    /**
     * Read a single type from a binary file.
     */
    fun readBinary(reader: Reader): T {
        resetFlags()
        while (true) {
            val code = reader.readUnsignedByte()
            if (code == 0) {
                break
            }
            val field = fields[code] ?: throw IllegalArgumentException("Unknown field opcode '$code'. Is it registered with 'add()' in the type decoder?")
            field.readBinary(reader, code)
            flag(code)
        }
        return create()
    }

    /**
     * Write a list of types to a binary file.
     */
    fun writeBinary(writer: Writer, types: List<T>) {
        for (type in types) {
            writeBinary(writer, type)
        }
    }

    /**
     * Write a single type to a binary file.
     */
    fun writeBinary(writer: Writer, type: T): Boolean {
        load(type)
        return writeBinary(writer)
    }

    /**
     * Write the decoders current values to a binary file.
     */
    fun writeBinary(writer: Writer): Boolean {
        var written = false
        for ((opcode, field) in fields.withIndex()) {
            if (field == null || opcode <= 0) {
                continue
            }
            if (field.writeBinary(writer, opcode)) {
                written = true
            }
        }
        if (written) {
            writer.writeByte(0)
        }
        return written
    }

    /**
     * Read a list of types from a config file.
     */
    fun readConfig(reader: ConfigReader, list: MutableList<T>) {
        val fields = fieldMap()
        while (reader.nextSection()) {
            list.add(readConfig(reader, fields))
        }
    }

    /**
     * Read a single type from a config file.
     */
    fun readConfig(reader: ConfigReader, fields: Map<String, TypeField> = fieldMap()): T {
        reset()
        val section = reader.section()
        (fields["[section]"] as? ValueField<String>)?.value = section
        while (reader.nextPair()) {
            val key = reader.key()
            val field = fields[key] ?: throw IllegalArgumentException("Unknown field '$key'. Is it registered in the type decoder?")
            field.readConfig(reader, key)
        }
        return create()
    }

    private fun fieldMap(): Map<String, TypeField> = (fields + extras).flatMap { field -> field?.keys?.map { key -> key to field } ?: emptyList() }.toMap()

    /**
     * Read a list of types from a binary file.
     */
    fun readConfig(reader: Reader, list: MutableList<T>) {
        while (reader.remaining > 0) {
            list.add(this@TypeDecoder.readBinary(reader))
        }
    }

    /**
     * Write a list of types to a config file.
     */
    fun writeConfig(writer: ConfigWriter, types: List<T>) {
        val section = findSectionField()
        for (type in types) {
            writeConfig(writer, type, section)
        }
    }

    /**
     * Write a single type to a config file.
     */
    fun writeConfig(writer: ConfigWriter, type: T, section: ValueField<String> = findSectionField()) {
        load(type)
        writeConfig(writer, section)
    }

    /**
     * Write the decoders current values to a config file.
     */
    fun writeConfig(writer: ConfigWriter, section: ValueField<String> = findSectionField()) {
        val fields = fields + extras
        writer.writeSection(section.value)
        val written = mutableSetOf<String>()
        for (field in fields) {
            if (field == null || field == section) {
                continue
            }
            for (key in field.keys) {
                if (!written.add(key)) {
                    continue
                }
                field.writeConfig(writer, key)
            }
        }
    }

    private fun findSectionField(): ValueField<String> = (fields + extras).firstOrNull { it?.keys?.contains("[section]") ?: false } as? ValueField<String> ?: throw IllegalArgumentException("No section field defined.")
}