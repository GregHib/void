package world.gregs.voidps.cache.type

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeSection
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.BooleanField
import world.gregs.voidps.cache.type.field.ByteField
import world.gregs.voidps.cache.type.field.ByteIntField
import world.gregs.voidps.cache.type.field.FixedField
import world.gregs.voidps.cache.type.field.IntArrayField
import world.gregs.voidps.cache.type.field.IntField
import world.gregs.voidps.cache.type.field.ListField
import world.gregs.voidps.cache.type.field.ListNullField
import world.gregs.voidps.cache.type.field.NullListNullField
import world.gregs.voidps.cache.type.field.NullBooleanField
import world.gregs.voidps.cache.type.field.NullIntField
import world.gregs.voidps.cache.type.field.NullListField
import world.gregs.voidps.cache.type.field.NullStringField
import world.gregs.voidps.cache.type.field.ParameterField
import world.gregs.voidps.cache.type.field.IndexedStringArrayField
import world.gregs.voidps.cache.type.field.ShortField
import world.gregs.voidps.cache.type.field.ShortIntField
import world.gregs.voidps.cache.type.field.SkipField
import world.gregs.voidps.cache.type.field.StringField
import world.gregs.voidps.cache.type.field.ValueField

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
abstract class TypeDecoder<T : Type> {
    /**
     * Maps opcodes to their corresponding fields for binary serialization.
     */
    val fields: LinkedHashMap<Int, TypeField> = linkedMapOf()

    /**
     * Creates a new instance of the Type using the current field values.
     */
    abstract fun create(): T

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

    fun string(key: String, default: String, opcode: Int) = register(opcode, StringField(key, default))

    fun string(key: String, default: String?, opcode: Int): NullStringField {
        if (default != null) {
            error = "Nullable string fields must have a null default value"
        }
        return register(opcode, NullStringField(key))
    }

    fun int(key: String, default: Int, opcode: Int) = register(opcode, IntField(key, default))

    fun <A : Any> fixed(key: String, default: A, value: A, field: ValueField<A>, opcode: Int) = register(opcode, FixedField(key, default, value, field))

    fun int(key: String, default: Int?, opcode: Int): NullIntField {
        if (default != null) {
            error = "Nullable int fields must have a null default value"
        }
        return register(opcode, NullIntField(key))
    }

    fun byte(key: String, default: Byte, opcode: Int) = register(opcode, ByteField(key, default))

    fun byte(key: String, default: Int, opcode: Int) = register(opcode, ByteIntField(key, default))

    fun short(key: String, default: Short, opcode: Int) = register(opcode, ShortField(key, default))

    fun short(key: String, default: Int, opcode: Int) = register(opcode, ShortIntField(key, default))

    fun skip(vararg opcodes: Int, amount: Int): SkipField {
        val field = SkipField({ amount })
        for (opcode in opcodes) {
            register(opcode, field)
        }
        return field
    }

    fun skip(vararg opcodes: Int, block: (Reader) -> Int): SkipField {
        val field = SkipField(block)
        for (opcode in opcodes) {
            register(opcode, field)
        }
        return field
    }

    fun bool(key: String, default: Boolean, opcode: Int) = register(opcode, BooleanField(key, default))

    fun bool(key: String, default: Boolean?, opcode: Int): NullBooleanField {
        if (default != null) {
            error = "Nullable bool fields must have a null default value"
        }
        return register(opcode, NullBooleanField(key))
    }

    fun intArray(key: String, default: IntArray?, opcode: Int) = register(opcode, IntArrayField(key, default, IntField()))

    fun stringArray(key: String, default: Array<String?>, opcodes: IntRange): IndexedStringArrayField {
        val field = IndexedStringArrayField(key, default, NullStringField(), opcodes.first)
        for (opcode in opcodes) {
            register(opcode, field)
        }
        return field
    }

    @JvmName("listAny")
    fun <T : Any> list(key: String, default: List<T>, opcode: Int, field: ValueField<T>) = register(opcode, ListField(key, default, field))

    @JvmName("listNull")
    fun <T : Any> list(key: String, default: List<T>?, opcode: Int, field: ValueField<T>): NullListField<T> {
        if (default != null) {
            error = "Nullable list fields must have a null default value"
        }
        return register(opcode, NullListField(key, default, field))
    }

    fun <T : Any> list(key: String, default: List<T?>, opcode: Int, field: ValueField<T?>) = register(opcode, ListNullField(key, default, field))

    fun <T : Any> list(key: String, default: List<T?>?, opcode: Int, field: ValueField<T?>): NullListNullField<T> {
        if (default != null) {
            error = "Nullable list fields must have a null default value"
        }
        return register(opcode, NullListNullField(key, default, field))
    }

    @JvmName("listInt")
    fun list(key: String, default: List<Int>, opcode: Int) = list(key, default, opcode, IntField())

    @JvmName("listIntNull")
    fun list(key: String, default: List<Int>?, opcode: Int) = list(key, default, opcode, IntField())

    @JvmName("listNullInt")
    fun list(key: String, default: List<Int?>, opcode: Int) = list(key, default, opcode, NullIntField())

    @JvmName("nullListInt")
    fun list(key: String, default: List<Int?>?, opcode: Int) = list(key, default, opcode, NullIntField())

    @JvmName("listBool")
    fun list(key: String, default: List<Boolean>, opcode: Int) = list(key, default, opcode, BooleanField())

    @JvmName("listBoolNull")
    fun list(key: String, default: List<Boolean>?, opcode: Int) = list(key, default, opcode, BooleanField())

    @JvmName("listNullBool")
    fun list(key: String, default: List<Boolean?>, opcode: Int) = list(key, default, opcode, NullBooleanField())

    @JvmName("nullListBool")
    fun list(key: String, default: List<Boolean?>?, opcode: Int) = list(key, default, opcode, NullBooleanField())

    @JvmName("listString")
    fun list(key: String, default: List<String>, opcode: Int) = list(key, default, opcode, StringField())

    @JvmName("listStringNull")
    fun list(key: String, default: List<String>?, opcode: Int) = list(key, default, opcode, StringField())

    @JvmName("listNullString")
    fun list(key: String, default: List<String?>, opcode: Int) = list(key, default, opcode, NullStringField())

    @JvmName("nullListString")
    fun list(key: String, default: List<String?>?, opcode: Int) = list(key, default, opcode, NullStringField())


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

    private inline fun <reified F : TypeField> register(opcode: Int, field: F): F {
        if (fields.containsKey(opcode)) {
            error = "Duplicate opcodes: $opcode"
        }
        fields[opcode] = field
        return field
    }

    /**
     *  Check post-compilation for errors or invalid fields.
     */
    fun check() {
        require(error == null) { error!! }
        val duplicateKeys = fields.values
            .flatMap { it.keys }
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
        for (field in fields.values) {
            field.reset()
        }
    }

    /**
     * Read a list of types from a config file.
     */
    fun read(reader: ConfigReader, list: MutableList<T>) {
        val fields = fieldMap()
        while (reader.nextSection()) {
            list.add(read(reader, fields))
        }
    }

    /**
     * Read a single type from a config file.
     */
    fun read(reader: ConfigReader, fields: Map<String, TypeField> = fieldMap()): T {
        reset()
        val section = reader.section()
        (fields["[section]"] as? StringField)?.value = section
        while (reader.nextPair()) {
            val key = reader.key()
            val field = fields[key] ?: throw IllegalArgumentException("Unknown field '$key'. Is it registered in the type decoder?")
            field.read(reader, key)
        }
        return create()
    }

    private fun fieldMap(): Map<String, TypeField> = fields.values.flatMap { field -> field.keys.map { key -> key to field } }.toMap()

    /**
     * Read a list of types from a binary file.
     */
    fun read(reader: Reader, list: MutableList<T>) {
        while (reader.remaining > 0) {
            list.add(this@TypeDecoder.read(reader))
        }
    }

    /**
     * Read a single type from a binary file.
     */
    fun read(reader: Reader): T {
        reset()
        while (true) {
            val code = reader.readUnsignedByte()
            if (code == 0) {
                break
            }
            val field = fields[code] ?: throw IllegalArgumentException("Unknown field opcode '$code'. Is it registered with 'add()' in the type decoder?")
            field.read(reader, code)
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
    fun writeBinary(writer: Writer, type: T) {
        var index = 0
        for (field in fields.values) {
            field.set(index, type.component(index++))
        }
        var written = false
        for ((opcode, field) in fields) {
            written = written or field.write(writer, opcode)
        }
        if (written) {
            writer.writeByte(0)
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
    fun writeConfig(writer: ConfigWriter, type: T, section: StringField = findSectionField()) {
        val fields = fields.values
        var index = 0
        for (field in fields) {
            field.set(index, type.component(index++))
        }
        writer.writeSection(section.value)
        for (field in fields) {
            if (field == section) {
                continue
            }
            for (key in field.keys) {
                field.write(writer, key)
            }
        }
    }

    private fun findSectionField(): StringField = fields.values.firstOrNull { it.keys.contains("[section]") } as? StringField ?: throw IllegalArgumentException("No section field defined.")
}