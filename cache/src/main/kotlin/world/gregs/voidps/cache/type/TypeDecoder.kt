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
 *     private val name = string("[section]", "default", opcode = 15)
 *     private val cost = int("cost", 0, opcode = 10)
 *
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
abstract class TypeDecoder<T : Type>(val size: Int = 256) {

    init {
        check(size <= 256) { "Field size cannot exceed 256: $size" }
    }

    abstract val id: ValueField<Int>

    /**
     * Maps opcodes to their corresponding fields for binary serialization.
     */
    val fields: Array<TypeField?> = arrayOfNulls(size)

    /**
     * Creates a new instance of the Type using the current field values.
     */
    abstract fun create(): T

    abstract fun load(type: T)

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

    fun bool(key: String, default: Boolean, opcode: Int) = register(opcode, key, default, BooleanCodec)

    fun boolLiteral(key: String, default: Boolean, value: Boolean, opcode: Int) = register(opcode, key, default, LiteralCodec(value, BooleanCodec))

    fun byte(key: String, default: Int, opcode: Int) = register(opcode, key, default, ByteCodec)

    fun byteArray(key: String, opcode: Int) = register(opcode, key, null, NullByteArrayCodec)

    fun ubyte(key: String, default: Int, opcode: Int) = register(opcode, key, default, UnsignedByteCodec)

    fun short(key: String, default: Int, opcode: Int) = register(opcode, key, default, ShortCodec)

    fun ushort(key: String, default: Int, opcode: Int) = register(opcode, key, default, UnsignedShortCodec)

    fun shortArray(key: String, opcode: Int) = register(opcode, key, null, NullShortArrayCodec)

    fun int(key: String, default: Int, opcode: Int) = register(opcode, key, default, IntCodec)

    fun intLiteral(key: String, default: Int, value: Int, opcode: Int) = register(opcode, key, default, LiteralCodec(value, IntCodec))

    fun intArray(key: String, opcode: Int) = register(opcode, key, null, NullIntArrayCodec)

    fun string(key: String, default: String, opcode: Int) = register(opcode, key, default, StringCodec)

    fun string(key: String, opcode: Int): ValueField<String?> {
        return register(opcode, key, null, NullStringCodec)
    }

    fun stringArray(key: String, default: Array<String>, opcode: Int) = register(opcode, key, default, ArrayCodec(StringCodec) { size, block -> Array(size, block) })

    fun stringArray(key: String, opcode: Int) = register(opcode, key, null, NullArrayCodec(StringCodec) { size, block -> Array(size, block) })

    fun indexedStringArray(key: String, default: Array<String?>, opcodes: IntRange): IndexedStringArrayField {
        val field = IndexedStringArrayField(key, default, opcodes.first)
        for (opcode in opcodes) {
            register(opcode, field)
        }
        return field
    }

    fun map(key: String, fields: Map<String, FieldCodec<out Any>>, opcode: Int) = register(opcode, key, null, NullMapCodec(fields))

    fun map(key: String, default: Map<String, Any>, fields: Map<String, FieldCodec<out Any>>, opcode: Int) = register(opcode, key, default, MapCodec(fields))

    inline fun <reified T> array(key: String, field: FieldCodec<T>, opcode: Int) = register(opcode, key, null, NullArrayCodec(field) { size, block -> Array(size, block) })

    inline fun <reified T> array(key: String, default: Array<T>, field: FieldCodec<T>, opcode: Int) = register(opcode, key, default, ArrayCodec(field) { size, block -> Array(size, block) })

    operator fun <T : Any?> FieldCodec<T>.invoke(key: String, default: T) = ValueField(key, default, this)

    fun <A, B> pair(first: ValueField<A>, second: ValueField<B>, opcode: Int) = register(opcode, FieldPair(first, second))

    fun <A, B, C> triple(first: ValueField<A>, second: ValueField<B>, third: ValueField<C>, opcode: Int) = register(opcode, FieldTriple(first, second, third))

    fun colours(original: String, modified: String, opcode: Int) = register(opcode, ColourField(original, modified))

    fun params(opcode: Int, block: ParameterBuilder.() -> Unit): ParameterField {
        val builder = ParameterBuilder()
        block.invoke(builder)
        return register(opcode, ParameterField(builder.paramIds, builder.params, builder.transforms, builder.transformIds, builder.renames, builder.originals))
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

    fun <T : Any?> register(opcode: Int, key: String, default: T, codec: FieldCodec<T>) = register(opcode, ValueField(key, default, codec))

    inline fun <reified F : TypeField> register(opcode: Int, field: F): F {
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
        val duplicateKeys = fields
            .flatMap { it?.keys ?: emptyList() }
            .groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
        require(duplicateKeys.isEmpty()) {
            "Duplicate field names: ${duplicateKeys.keys}"
        }
    }

    fun join(other: TypeDecoder<T>) {
        for (i in fields.indices) {
            val field = fields[i] ?: continue
            field.join(other.fields[i]!!)
        }
    }

    /**
     * Reset all field values to their default state.
     */
    fun reset() {
        for (field in fields) {
            field?.reset()
        }
    }

    private val resetArray = IntArray(size)
    private var resetIndex = 0

    fun resetFlags() {
        val resetArray = resetArray
        val fields = fields
        for (idx in 0 until resetIndex) {
            fields[resetArray[idx]]!!.reset()
        }
        resetIndex = 0
    }

    fun flag(code: Int) {
        resetArray[resetIndex++] = code
    }

    /**
     * Read a single type from a binary file.
     */
    fun readBinary(reader: Reader): T {
        resetFlags()
        loadBinary(reader)
        return create()
    }

    fun loadBinary(reader: Reader) {
        while (true) {
            val code = reader.readUnsignedByte()
            if (code == 0) {
                break
            }
            val field = fields[code] ?: throw IllegalArgumentException("Unknown field opcode '$code'. Is it registered with 'add()' in the type decoder?")
            field.readBinary(reader, code)
            flag(code)
        }
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
    fun writeBinary(writer: Writer, official: Boolean = false): Boolean {
        var written = false
        for ((opcode, field) in fields.withIndex()) {
            if (field == null || (opcode > 249 && official)) {
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
        loadConfig(reader, fields)
        return create()
    }

    fun loadConfig(reader: ConfigReader, fields: Map<String, TypeField> = fieldMap()) {
        val section = reader.section()
        (fields["[section]"] as? ValueField<String>)?.value = section
        while (reader.nextPair()) {
            val key = reader.key()
            val field = fields[key] ?: throw IllegalArgumentException("Unknown field '$key'. Is it registered in the type decoder?")
            field.readConfig(reader, key)
        }
    }

    fun fieldMap(): Map<String, TypeField> = fields.flatMap { field -> field?.keys?.map { key -> key to field } ?: emptyList() }.toMap()

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
        val fields = fields
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
        return "TypeDecoder(id=$id, ${fields.filterNotNull().joinToString { "${it.keys.first()}=${it}" }})"
    }

    private fun findSectionField(): ValueField<String> = fields.firstOrNull { it?.keys?.contains("[section]") ?: false } as? ValueField<String> ?: throw IllegalArgumentException("No section field defined.")
}