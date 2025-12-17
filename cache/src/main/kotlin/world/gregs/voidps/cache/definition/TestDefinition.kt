package world.gregs.voidps.cache.definition

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.list
import world.gregs.config.writeKey
import world.gregs.config.writeSection
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import java.io.BufferedInputStream
import java.io.StringWriter

data class TestDefinition(
    val name: String = "null",
    val size: Int = 1,
    val options: List<String?> = listOf("Test", null, null),
    val list: List<String> = listOf("Test", "Test2"),
    val original: IntArray? = null,
    val modified: IntArray? = null,
) : Type {
    companion object : DefinitionFields<TestDefinition>() {
        override val empty = TestDefinition()

        private val NAME = string("[section]", empty.name, opcode = 1)
        private val SIZE = int("size", empty.size, opcode = 4)
        private val OPTIONS = list("options", empty.options, opcode = 3)
        private val LIST = list("list", empty.list, opcode = 5)
        private val NUMBERS = intArray("numbers", empty.original, opcode = 2)

        override fun create() = TestDefinition(
            name = NAME.value,
            size = SIZE.value,
            list = LIST.value,
            options = OPTIONS.value,
            original = NUMBERS.value,
        )

        @JvmStatic
        fun main(args: Array<String>) {
            val string = """
                [test_name]
                size = 2
                options = ["test", "null"]
            """.trimIndent()
            val reader = ConfigReader(BufferedInputStream(string.byteInputStream()))
            val configs = mutableListOf<TestDefinition>()
            check()
            read(reader, configs)
            println("Read $configs")
            val stringWriter = StringWriter()
            writeConfig(stringWriter, configs)
            println("Wrote: ${stringWriter}")

            val binary = BufferWriter(200)
            writeBinary(binary, configs)
            println(read(BufferReader(binary.toArray())))
        }
    }
}

class RecolourableField(val originalKey: String, val modifiedKey: String) : Field(listOf(originalKey, modifiedKey)) {

    var originalColours: ShortArray? = null
    var modifiedColours: ShortArray? = null

    override fun write(writer: Writer, opcode: Int): Boolean {
        val originalColours = originalColours
        val modifiedColours = modifiedColours
        if (originalColours != null && modifiedColours != null) {
            writer.writeByte(opcode)
            writer.writeByte(originalColours.size)
            for (i in originalColours.indices) {
                writer.writeShort(originalColours[i].toInt())
                writer.writeShort(modifiedColours[i].toInt())
            }
            return true
        }
        return false
    }

    override fun read(reader: Reader) {
        val size = reader.readUnsignedByte()
        originalColours = ShortArray(size)
        modifiedColours = ShortArray(size)
        for (count in 0 until size) {
            originalColours!![count] = reader.readShort().toShort()
            modifiedColours!![count] = reader.readShort().toShort()
        }
    }

    override fun read(reader: ConfigReader, key: String) {
        when (key) {
            originalKey -> {
                val list = mutableListOf<Short>()
                while (reader.nextElement()) {
                    list.add(reader.int().toShort())
                }
                originalColours = ShortArray(list.size) { list[it] }
            }
            modifiedKey -> {
                val list = mutableListOf<Short>()
                while (reader.nextElement()) {
                    list.add(reader.int().toShort())
                }
                modifiedColours = ShortArray(list.size) { list[it] }
            }
        }
    }

    override fun write(writer: ConfigWriter, key: String) {
        when (key) {
            originalKey -> {
                val original = originalColours ?: return
                writer.list(original.size) {
                    write(original[it].toInt())
                }
            }
            modifiedKey -> {
                val modified = modifiedColours ?: return
                writer.list(modified.size) {
                    write(modified[it].toInt())
                }
            }
        }
    }

    override fun reset() {
        originalColours = null
        modifiedColours = null
    }

    override fun set(index: Int, value: Any?) {
        when (index) {
            1 -> originalColours = value as ShortArray?
            2 -> modifiedColours = value as ShortArray?
        }
    }
}


abstract class DefinitionFields<D : Type> {
    abstract val empty: D
    val fields: LinkedHashMap<Int, Field> = linkedMapOf()
    abstract fun create(): D
    var error: String? = null

    fun string(key: String, default: String, opcode: Int) = register(opcode, StringField(key, default))

    fun string(key: String, default: String?, opcode: Int): NullStringField {
        if (default != null) {
            error = "Nullable string fields must have a null default value"
        }
        return register(opcode, NullStringField(key))
    }

    fun int(key: String, default: Int, opcode: Int) = register(opcode, IntField(key, default))

    fun int(key: String, default: Int?, opcode: Int): NullIntField {
        if (default != null) {
            error = "Nullable int fields must have a null default value"
        }
        return register(opcode, NullIntField(key))
    }

    fun bool(key: String, default: Boolean, opcode: Int) = register(opcode, BooleanField(key, default))

    fun bool(key: String, default: Boolean?, opcode: Int): NullBooleanField {
        if (default != null) {
            error = "Nullable bool fields must have a null default value"
        }
        return register(opcode, NullBooleanField(key))
    }

    fun intArray(key: String, default: IntArray?, opcode: Int) = register(opcode, IntArrayField(key, default, IntField()))

    @JvmName("listAny")
    fun <T : Any> list(key: String, default: List<T>, opcode: Int, field: SingleField<T>) = register(opcode, ListField(key, default, field))

    @JvmName("listNull")
    fun <T : Any> list(key: String, default: List<T>?, opcode: Int, field: SingleField<T>): NullListField<T> {
        if (default != null) {
            error = "Nullable list fields must have a null default value"
        }
        return register(opcode, NullListField(key, default, field))
    }

    fun <T : Any> list(key: String, default: List<T?>, opcode: Int, field: SingleField<T?>) = register(opcode, ListNullField(key, default, field))

    fun <T : Any> list(key: String, default: List<T?>?, opcode: Int, field: SingleField<T?>): NestedNullListField<T> {
        if (default != null) {
            error = "Nullable list fields must have a null default value"
        }
        return register(opcode, NestedNullListField(key, default, field))
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

    private inline fun <reified F : Field> register(opcode: Int, field: F): F {
        if (fields.containsKey(opcode)) {
            error = "Duplicate opcodes: $opcode"
        }
        fields[opcode] = field
        return field
    }

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

    private fun fieldMap(): Map<String, Field> = fields.values.flatMap { field -> field.keys.map { key -> key to field } }.toMap()

    fun reset() {
        for (field in fields.values) {
            field.reset()
        }
    }

    fun read(reader: ConfigReader, list: MutableList<D>) {
        val fields = fieldMap()
        while (reader.nextSection()) {
            list.add(read(reader, fields))
        }
    }

    fun read(reader: ConfigReader, fields: Map<String, Field> = fieldMap()): D {
        reset()
        val section = reader.section()
        (fields["[section]"] as? StringField)?.value = section
        while (reader.nextPair()) {
            val key = reader.key()
            val field = fields[key] ?: throw IllegalArgumentException("Unknown field $key")
            field.read(reader, key)
        }
        return create()
    }

    fun read(reader: Reader, list: MutableList<D>) {
        while (reader.remaining > 0) {
            list.add(this@DefinitionFields.read(reader))
        }
    }

    fun read(reader: Reader): D {
        reset()
        while (true) {
            val code = reader.readByte()
            if (code == 0) {
                break
            }
            val field = fields[code] ?: throw IllegalArgumentException("Unknown field $code")
            field.read(reader)
        }
        return create()
    }

    fun <D : Type> writeBinary(writer: Writer, definitions: List<D>) {
        for (definition in definitions) {
            writeBinary(writer, definition)
        }
    }

    fun <D : Type> writeBinary(writer: Writer, definition: D) {
        var index = 0
        for (field in fields.values) {
            field.set(index, definition.component(index++))
        }
        var written = false
        for ((opcode, field) in fields) {
            written = written or field.write(writer, opcode)
        }
        if (written) {
            writer.writeByte(0)
        }
    }

    fun writeConfig(writer: ConfigWriter, definitions: List<D>) {
        val section = findSectionField()
        for (definition in definitions) {
            writeConfig(writer, definition, section)
        }
    }

    fun writeConfig(writer: ConfigWriter, definition: D, section: StringField = findSectionField()) {
        val fields = fields.values
        var index = 0
        for (field in fields) {
            field.set(index, definition.component(index++))
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

interface Type {
    fun component1(): Any? = null
    fun component2(): Any? = null
    fun component3(): Any? = null
    fun component4(): Any? = null
    fun component5(): Any? = null
    fun component6(): Any? = null
    fun component7(): Any? = null
    fun component8(): Any? = null
    fun component9(): Any? = null
    fun component10(): Any? = null
    fun component11(): Any? = null
    fun component12(): Any? = null
    fun component13(): Any? = null
    fun component14(): Any? = null
    fun component15(): Any? = null
    fun component16(): Any? = null
    fun component17(): Any? = null
    fun component18(): Any? = null
    fun component19(): Any? = null
    fun component20(): Any? = null

    fun component(index: Int) = when (index) {
        0 -> component1()
        1 -> component2()
        2 -> component3()
        3 -> component4()
        4 -> component5()
        5 -> component6()
        6 -> component7()
        7 -> component8()
        8 -> component9()
        9 -> component10()
        10 -> component11()
        11 -> component12()
        12 -> component13()
        13 -> component14()
        14 -> component15()
        15 -> component16()
        16 -> component17()
        17 -> component18()
        18 -> component19()
        19 -> component20()
        else -> throw IllegalArgumentException("Invalid index $index")
    }
}

abstract class Field(
    val keys: List<String>,
) {
    abstract fun write(writer: Writer, opcode: Int): Boolean
    abstract fun read(reader: Reader)
    abstract fun read(reader: ConfigReader, key: String)
    abstract fun write(writer: ConfigWriter, key: String)
    abstract fun reset()
    abstract fun set(index: Int, value: Any?)

}

abstract class SingleField<T : Any?>(
    key: String,
    val default: T,
) : Field(listOf(key)) {

    internal var value: T = default

    @Suppress("UNCHECKED_CAST")
    override fun set(index: Int, value: Any?) {
        this.value = value as T
    }

    abstract fun readConfig(reader: ConfigReader): T
    abstract fun writeConfig(writer: ConfigWriter, value: T)

    abstract fun readBinary(reader: Reader): T
    abstract fun writeBinary(writer: Writer, value: T)

    override fun write(writer: Writer, opcode: Int): Boolean {
        if (value != default) {
            writer.writeByte(opcode)
            writeBinary(writer, value)
            return true
        }
        return false
    }

    override fun write(writer: ConfigWriter, key: String) {
        if (value != default) {
            writer.writeKey(key)
            writeConfig(writer, value)
            writer.write("\n")
        }
    }

    override fun read(reader: Reader) {
        value = readBinary(reader)
    }

    override fun read(reader: ConfigReader, key: String) {
        value = readConfig(reader)
    }

    override fun reset() {
        value = default
    }
}

class IntField(
    key: String = "",
    default: Int = 0,
) : SingleField<Int>(key, default) {
    override fun readBinary(reader: Reader) = reader.readInt()
    override fun writeBinary(writer: Writer, value: Int) = writer.writeInt(value)
    override fun readConfig(reader: ConfigReader): Int = reader.int()
    override fun writeConfig(writer: ConfigWriter, value: Int) = writer.writeValue(value)
}

class NullIntField(
    key: String = "",
) : SingleField<Int?>(key, default = null) {
    override fun readBinary(reader: Reader) = reader.readInt()
    override fun writeBinary(writer: Writer, value: Int?) = writer.writeInt(value ?: throw IllegalArgumentException("Binary int fields cannot be null."))
    override fun readConfig(reader: ConfigReader) = if (reader.peek == '"') {
        check(reader.string() == "null")
        null
    } else {
        reader.int()
    }

    override fun writeConfig(writer: ConfigWriter, value: Int?) = writer.writeValue(value)
}

class BooleanField(
    key: String = "",
    default: Boolean = false,
) : SingleField<Boolean>(key, default) {
    override fun readBinary(reader: Reader) = reader.readBoolean()
    override fun writeBinary(writer: Writer, value: Boolean) = writer.writeByte(value)
    override fun readConfig(reader: ConfigReader): Boolean = reader.boolean()
    override fun writeConfig(writer: ConfigWriter, value: Boolean) = writer.writeValue(value)
}

class NullBooleanField(
    key: String = "",
) : SingleField<Boolean?>(key, default = null) {
    override fun readBinary(reader: Reader): Boolean? {
        if (reader.peek() == -1) {
            reader.skip(1)
            return null
        }
        return reader.readBoolean()
    }

    override fun writeBinary(writer: Writer, value: Boolean?) = if (value == null) {
        writer.writeByte(-1)
    } else {
        writer.writeByte(value)
    }

    override fun readConfig(reader: ConfigReader): Boolean? = if (reader.peek == '"') {
        check(reader.string() == "null")
        null
    } else {
        reader.boolean()
    }

    override fun writeConfig(writer: ConfigWriter, value: Boolean?) = writer.writeValue(value)
}

class StringField(
    key: String = "",
    default: String = "",
) : SingleField<String>(key, default) {
    override fun readBinary(reader: Reader) = reader.readString()
    override fun writeBinary(writer: Writer, value: String) = writer.writeString(value)
    override fun readConfig(reader: ConfigReader) = reader.string()
    override fun writeConfig(writer: ConfigWriter, value: String) = writer.writeValue(value)
}

class NullStringField(
    key: String = "",
) : SingleField<String?>(key, default = null) {
    override fun readBinary(reader: Reader): String? {
        if (reader.peek() == -1) {
            reader.skip(1)
            return null
        }
        return reader.readString()
    }

    override fun writeBinary(writer: Writer, value: String?) = if (value == null) {
        writer.writeByte(-1)
    } else {
        writer.writeString(value)
    }

    override fun readConfig(reader: ConfigReader): String? {
        val value = reader.string()
        if (value == "null") {
            return null
        }
        return value
    }

    override fun writeConfig(writer: ConfigWriter, value: String?) = writer.writeValue(value)
}

class ListField<T : Any>(
    key: String,
    default: List<T>,
    val field: SingleField<T>,
) : SingleField<List<T>>(key, default) {
    override fun readBinary(reader: Reader): List<T> {
        val length = reader.readByte()
        val list = mutableListOf<T>()
        for (i in 0 until length) {
            list.add(field.readBinary(reader))
        }
        return list
    }

    override fun writeBinary(writer: Writer, value: List<T>) {
        writer.writeByte(value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): List<T> {
        val list = mutableListOf<T>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return list
    }

    override fun writeConfig(writer: ConfigWriter, value: List<T>) {
        writer.writeValue(value)
    }
}

class NullListField<T : Any>(
    key: String,
    default: List<T>?,
    val field: SingleField<T>,
) : SingleField<List<T>?>(key, default) {
    override fun readBinary(reader: Reader): List<T> {
        val length = reader.readByte()
        val list = mutableListOf<T>()
        for (i in 0 until length) {
            list.add(field.readBinary(reader))
        }
        return list
    }

    override fun writeBinary(writer: Writer, value: List<T>?) {
        if (value == null || value.isEmpty()) {
            writer.writeByte(0)
            return
        }
        writer.writeByte(value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): List<T> {
        val list = mutableListOf<T>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return list
    }

    override fun writeConfig(writer: ConfigWriter, value: List<T>?) {
        writer.writeValue(value)
    }
}

class NestedNullListField<T : Any>(
    key: String,
    default: List<T?>?,
    val field: SingleField<T?>,
) : SingleField<List<T?>?>(key, default) {
    override fun readBinary(reader: Reader): List<T?> {
        val length = reader.readByte()
        val list = mutableListOf<T?>()
        for (i in 0 until length) {
            list.add(field.readBinary(reader))
        }
        return list
    }

    override fun writeBinary(writer: Writer, value: List<T?>?) {
        if (value == null || value.isEmpty()) {
            writer.writeByte(0)
            return
        }
        writer.writeByte(value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): List<T?> {
        val list = mutableListOf<T?>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return list
    }

    override fun writeConfig(writer: ConfigWriter, value: List<T?>?) {
        writer.writeValue(value)
    }
}

class ListNullField<T : Any>(
    key: String,
    default: List<T?>,
    val field: SingleField<T?>,
) : SingleField<List<T?>>(key, default) {
    override fun readBinary(reader: Reader): List<T?> {
        val length = reader.readByte()
        val list = mutableListOf<T?>()
        for (i in 0 until length) {
            list.add(field.readBinary(reader))
        }
        return list
    }

    override fun writeBinary(writer: Writer, value: List<T?>) {
        if (value.isEmpty()) {
            writer.writeByte(0)
            return
        }
        writer.writeByte(value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): List<T?> {
        val list = mutableListOf<T?>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return list
    }

    override fun writeConfig(writer: ConfigWriter, value: List<T?>) {
        writer.writeValue(value)
    }
}

class IntArrayField(
    key: String = "",
    default: IntArray? = null,
    val field: IntField,
) : SingleField<IntArray?>(key, default) {
    override fun readBinary(reader: Reader): IntArray? {
        val size = reader.readByte()
        if (size == 0 && default == null) {
            return null
        }
        return IntArray(size) { field.readBinary(reader) }
    }

    override fun writeBinary(writer: Writer, value: IntArray?) {
        if (value == null || value.isEmpty()) {
            writer.writeByte(0)
            return
        }
        writer.writeByte(value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): IntArray {
        val list = mutableListOf<Int>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return list.toIntArray()
    }

    override fun writeConfig(writer: ConfigWriter, value: IntArray?) {
        writer.writeValue(value)
    }
}