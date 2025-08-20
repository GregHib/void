import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import java.io.Reader
import java.io.Writer

private object NoOpEncoder : AbstractEncoder() {
    override val serializersModule: SerializersModule = EmptySerializersModule()

    public override fun encodeValue(value: Any): Unit = Unit

    override fun encodeNull(): Unit = Unit

    override fun encodeBoolean(value: Boolean): Unit = Unit
    override fun encodeByte(value: Byte): Unit = Unit
    override fun encodeShort(value: Short): Unit = Unit
    override fun encodeInt(value: Int): Unit = Unit
    override fun encodeLong(value: Long): Unit = Unit
    override fun encodeFloat(value: Float): Unit = Unit
    override fun encodeDouble(value: Double): Unit = Unit
    override fun encodeChar(value: Char): Unit = Unit
    override fun encodeString(value: String): Unit = Unit
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int): Unit = Unit
}

private val byteArraySerializer = serializer<ByteArray>()

@ExperimentalSerializationApi
class DataOutputEncoder :
    Encoder,
    CompositeEncoder {
    private var firstElement = true
    private var inMap = false
    private var rootMap = false
    private var mapExpectingKey = true
    lateinit var writer: Writer
    private var depth: Int = 0
    private var mapDepth: Int = 0
    private val mapExpectingKeyStack = ArrayDeque<Boolean>()

    fun set(writer: Writer) {
        this.writer = writer
        depth = 0
        mapDepth = 0
        firstElement = true
        inMap = false
        rootMap = false
        mapExpectingKey = true
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
//        println(descriptor.serialName)
        firstElement = true
        when (descriptor.kind) {
            StructureKind.CLASS, StructureKind.OBJECT -> {
                if (depth > 1) {
                    writer.write("{")
                }
                depth++
            } // only wrap nested objects
            StructureKind.LIST -> {
                writer.write("[")
                depth++
            }
            StructureKind.MAP -> {
                if (depth > 1) {
                    writer.write("{")
                    mapDepth++
                }
                mapExpectingKeyStack.addLast(true)
                depth++
            }
            else -> {}
        }
        return this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        when (descriptor.kind) {
            StructureKind.CLASS, StructureKind.OBJECT -> {
                depth--
                if (depth > 1) {
                    writer.write("}")
                }
            }
            StructureKind.LIST -> {
                depth--
                writer.write("]")
            }
            StructureKind.MAP -> {
                depth--
                if (depth > 1) {
                    writer.write("}")
                    mapDepth--
                }
                mapExpectingKeyStack.removeLast()
            }
            else -> {}
        }
    }

    private fun encodeSeparator() {
        if (!firstElement) {
            if (depth == 1 || mapDepth != 0) {
                writer.write("\n") // root level = newline
            } else {
                writer.write(", ") // nested = comma
            }
        }
        firstElement = false
    }

    private fun encodeDescriptor(descriptor: SerialDescriptor, index: Int) {
        encodeSeparator()
        if (descriptor.kind != StructureKind.LIST) {
            // normal named field
            writer.write(descriptor.getElementName(index))
            writer.write(" = ")
        }
    }

    override fun encodeInlineElement(
        descriptor: SerialDescriptor,
        index: Int,
    ): Encoder = encodeInline(descriptor.getElementDescriptor(index))

    override fun <T : Any?> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T,
    ) {
        when (descriptor.kind) {
            StructureKind.LIST -> {
                encodeSeparator()
                encodeSerializableValue(serializer, value)
            }
            StructureKind.MAP -> {
//                println("Encode ${descriptor.serialName} $mapExpectingKey ${value}")
                val expectingKey = mapExpectingKeyStack.last()
                if (expectingKey) {
                    if (!firstElement) {
                        if (mapDepth == 0) {
                            writer.write("\n")
                        } else {
                            writer.write(", ") // nested = comma
                        }
                    }
                    firstElement = false
                    // keys are always strings
//                        println("Encode $value")
                    if (value is String) writer.write(value) else encodeSerializableValue(serializer, value)
                    writer.write(" = ")
                } else {
                    encodeSerializableValue(serializer, value)
                }
                mapExpectingKeyStack[mapExpectingKeyStack.lastIndex] = !expectingKey
            }
            else -> {
                val kind = serializer.descriptor.kind
                if ((kind == StructureKind.MAP || kind == StructureKind.CLASS || kind == StructureKind.OBJECT) && depth <= 1) {
                    writer.write("\n\n")
                    writer.write("[")
                    writer.write(descriptor.getElementName(index))
                    writer.write("]")
                    writer.write("\n")
                } else {
                    encodeSeparator()
                    writer.write(descriptor.getElementName(index))
                    writer.write(" = ")
                }
                encodeSerializableValue(serializer, value)
            }
        }
    }

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?,
    ) {
        encodeNullableSerializableValue(serializer, value)
    }

    override val serializersModule: SerializersModule = EmptySerializersModule()

    // Elements

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        encodeDescriptor(descriptor, index)
        encodeBoolean(value)
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        encodeDescriptor(descriptor, index)
        encodeByte(value)
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        encodeDescriptor(descriptor, index)
        encodeShort(value)
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        encodeDescriptor(descriptor, index)
        encodeInt(value)
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        encodeDescriptor(descriptor, index)
        encodeLong(value)
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        encodeDescriptor(descriptor, index)
        encodeFloat(value)
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        encodeDescriptor(descriptor, index)
        encodeDouble(value)
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        encodeDescriptor(descriptor, index)
        encodeChar(value)
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        encodeDescriptor(descriptor, index)
        encodeString(value)
    }

    // Scalars

    override fun encodeBoolean(value: Boolean) = writer.write(value.toString())
    override fun encodeByte(value: Byte) = writer.write(value.toString())
    override fun encodeShort(value: Short) = writer.write(value.toString())
    override fun encodeInt(value: Int) = writer.write(value.toString())
    override fun encodeLong(value: Long) = writer.write(value.toString())
    override fun encodeFloat(value: Float) = writer.write(value.toString())
    override fun encodeDouble(value: Double) = writer.write(value.toString())
    override fun encodeChar(value: Char) = writer.write("'")
    override fun encodeString(value: String) {
        writer.write("\"")
        writer.write(value)
        writer.write("\"")
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        writer.write("\"")
        writer.write(enumDescriptor.getElementName(index))
        writer.write("\"")
    }

    override fun encodeNull() = writer.write("null")

    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        if (serializer.descriptor == byteArraySerializer.descriptor) {
            encodeByteArray(value as ByteArray)
        } else {
            super.encodeSerializableValue(serializer, value)
        }
    }

    private fun encodeByteArray(bytes: ByteArray) {
        writer.write(bytes.joinToString(prefix = "[", postfix = "]"))
    }
}

@ExperimentalSerializationApi
fun <T> encodeTo(writer: Writer, serializer: SerializationStrategy<T>, value: T) {
    val encoder = DataOutputEncoder()
    encoder.set(writer)
    encoder.encodeSerializableValue(serializer, value)
}

@ExperimentalSerializationApi
inline fun <reified T> encodeTo(writer: Writer, value: T) = encodeTo(writer, serializer(), value)

@OptIn(ExperimentalSerializationApi::class)
class DataInputDecoder(
    private val reader: Reader,
    private var elementsCount: Int = 0,
) : AbstractDecoder() {
    private var elementIndex = 0
    override val serializersModule: SerializersModule = EmptySerializersModule

    private var pushback: Int? = null

    private fun nextChar(): Int {
        val c = pushback ?: reader.read()
        pushback = null
        return c
    }

    private fun peekChar(): Int {
        if (pushback == null) pushback = reader.read()
        return pushback!!
    }

    private fun skipWhitespace() {
        while (true) {
            val c = peekChar()
            if (c == -1 || !c.toChar().isWhitespace()) return
            nextChar()
        }
    }

    private fun readWord(): String {
        skipWhitespace()
        val sb = StringBuilder()
        while (true) {
            val c = peekChar()
            if (c == -1 || !(c.toChar().isLetterOrDigit() || c.toChar() in "._")) break
            sb.append(c.toChar())
            nextChar()
        }
        return sb.toString()
    }

    private fun readQuoted(): String {
        skipWhitespace()
        if (nextChar().toChar() != '"') error("Expected '\"'")
        val sb = StringBuilder()
        while (true) {
            val c = nextChar()
            if (c == -1) error("Unterminated string")
            if (c.toChar() == '"') break
            sb.append(c.toChar())
        }
        return sb.toString()
    }

    private fun readNumber(): String {
        skipWhitespace()
        val sb = StringBuilder()
        while (true) {
            val c = peekChar()
            if (c == -1 || !(c.toChar().isDigit() || c.toChar() in ".-")) break
            sb.append(c.toChar())
            nextChar()
        }
        return sb.toString()
    }

    // --- AbstractDecoder overrides ---

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex == elementsCount) return CompositeDecoder.DECODE_DONE
        return elementIndex++
    }

    override fun decodeString(): String {
        skipWhitespace()
        return if (peekChar().toChar() == '"') readQuoted() else readWord()
    }

    override fun decodeInt(): Int = readNumber().toInt()
    override fun decodeLong(): Long = readNumber().toLong()
    override fun decodeFloat(): Float = readNumber().toFloat()
    override fun decodeDouble(): Double = readNumber().toDouble()
    override fun decodeBoolean(): Boolean = readWord().toBoolean()
    override fun decodeChar(): Char = decodeString().single()

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val name = decodeString()
        return enumDescriptor.getElementIndex(name)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        println("${descriptor.serialName} ${descriptor.elementNames.toList()} ${descriptor.elementsCount}")
        // For simplicity, wrap the *same* reader, relying on serializer to consume in order
        return DataInputDecoder(reader, descriptor.elementsCount)
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        println(deserializer.descriptor)
        return super.decodeSerializableValue(deserializer)
    }

    override fun decodeSequentially(): Boolean = false
}

// @Serializable
// data class Project(
//    val name: String,
//    val owners: List<User>,
//    val votes: Int,
//    val array: IntArray,
//    var variables: Map<String, Int>,
//    val social: Social,
//    var mapMap: Map<String, Map<String, Int>>
// )
//
// @Serializable
// data class Social(val friends: Map<String, String>)
//
// @Serializable
// data class User(val name: String)

// @OptIn(ExperimentalSerializationApi::class)
// fun main() {
//    val data = Project(
//        name = "kotlinx.serialization",
//        owners = listOf(User("kotlin"), User("jetbrains")),
//        votes = 9000,
//        array = intArrayOf(1, 2, 3, 4),
//        variables = mapOf("onety" to 1, "twoty" to 2),
//        social = Social(mapOf("friend" to "rank", "friend2" to "major")),
//        mapMap = mapOf("one" to mapOf("1" to 1, "1.2" to 1), "two" to mapOf("2" to 2))
//    )
//
//    val writer = StringWriter()
//    val serializer = serializer<Project>()
//    val encoder = DataOutputEncoder()
//    val duration = measureTime {
//        encoder.set(writer)
//        encoder.encodeSerializableValue(serializer, data)
//    }
//    println(writer.toString())
//    println(duration.inWholeMilliseconds)
//
//    var (string2, time2) = measureTimedValue {
//        Config.stringWriter {
//            writePair("name", data.name)
//            writeKey("owners")
//            list(data.owners.size) { index ->
//                map(setOf("name")) {
//                    write(data.owners[index].name)
//                }
//            }
//            write("\n")
//            writePair("vote", data.votes)
//            writePair("array", data.array)
//        }
//    }
//
//    val reader = StringReader(
//        """
//        name = "kotlinx.serialization"
//        owners = [{name = "kotlin"}, {name = "jetbrains"}]
//        votes = 9000
//        array = [1, 2, 3, 4]
//
//        [variables]
//        onety = 1
//        twoty = 2
//
//        [social]
//        friends = {friend = "rank", friend2 = "major"}
//
//        [mapMap]
//        one = {1 = 1, 1.2 = 1}
//        two = {2 = 2}
//    """.trimIndent()
//    )
//    val decoder = DataInputDecoder(reader)
//    decoder.decodeSerializableValue(serializer)
// //    println(string2)
// //    println(time2.inWholeMilliseconds)
// }
