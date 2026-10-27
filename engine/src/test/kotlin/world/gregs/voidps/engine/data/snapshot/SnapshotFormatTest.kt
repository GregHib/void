package world.gregs.voidps.engine.data.snapshot

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class SnapshotFormatTest {

    enum class Colour { Red, Green, Blue }

    @Serializable
    data class Sample(
        val flag: Boolean,
        val byte: Byte,
        val short: Short,
        val int: Int,
        val long: Long,
        val float: Float,
        val double: Double,
        val char: Char,
        val text: String,
        val maybe: String?,
        val colour: Colour,
        val list: List<Int>,
        val map: Map<String, Int>,
        val nested: Nested?,
        val ints: IntArray,
    ) {
        override fun equals(other: Any?) = other is Sample && flag == other.flag && byte == other.byte && short == other.short && int == other.int && long == other.long &&
            float == other.float && double == other.double && char == other.char && text == other.text && maybe == other.maybe && colour == other.colour &&
            list == other.list && map == other.map && nested == other.nested && ints.contentEquals(other.ints)

        override fun hashCode() = int
    }

    @Serializable
    data class Nested(val name: String, val values: List<Nested> = emptyList())

    @Serializable
    data class One(val a: Int, val b: String)

    @Serializable
    data class Two(val a: Int, val b: String, val c: Int)

    @Test
    fun `Round trip every kind of value`() {
        val sample = Sample(
            flag = true,
            byte = -4,
            short = 300,
            int = -7,
            long = Long.MIN_VALUE,
            float = 1.5f,
            double = 2.25,
            char = 'x',
            text = "héllo",
            maybe = null,
            colour = Colour.Blue,
            list = listOf(1, 2, 3),
            map = mapOf("a" to 1, "b" to 2),
            nested = Nested("root", listOf(Nested("child"))),
            ints = intArrayOf(5, 6),
        )
        val bytes = SnapshotFormat.encodeToByteArray(Sample.serializer(), sample)
        assertEquals(sample, SnapshotFormat.decodeFromByteArray(Sample.serializer(), bytes))
        val other = sample.copy(maybe = "set", nested = null)
        assertEquals(other, SnapshotFormat.decodeFromByteArray(Sample.serializer(), SnapshotFormat.encodeToByteArray(Sample.serializer(), other)))
    }

    @Test
    fun `Collections and nullables`() {
        val serializer = ListSerializer(MapSerializer(String.serializer(), Int.serializer().nullable))
        val value = listOf(mapOf("a" to 1, "b" to null), emptyMap())
        assertEquals(value, SnapshotFormat.decodeFromByteArray(serializer, SnapshotFormat.encodeToByteArray(serializer, value)))
    }

    private fun described(name: String, vararg elements: Pair<String, Boolean>) = buildClassSerialDescriptor(name) {
        for ((element, nullable) in elements) {
            element(element, if (nullable) String.serializer().nullable.descriptor else String.serializer().descriptor)
        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun enumeration(name: String, vararg entries: String) = buildSerialDescriptor(name, SerialKind.ENUM) {
        for (entry in entries) {
            element(entry, buildSerialDescriptor("$name.$entry", StructureKind.OBJECT))
        }
    }

    @Test
    fun `Fingerprint is stable`() {
        assertEquals(described("Type", "a" to false, "b" to false).fingerprint(), described("Type", "a" to false, "b" to false).fingerprint())
        assertEquals(One.serializer().descriptor.fingerprint(), One.serializer().descriptor.fingerprint())
        assertEquals(SnapshotFormat.codec(One.serializer()).schema, SnapshotFormat.codec(One.serializer()).schema)
    }

    @Test
    fun `Adding a field changes the fingerprint`() {
        assertNotEquals(described("Type", "a" to false).fingerprint(), described("Type", "a" to false, "b" to false).fingerprint())
        assertNotEquals(One.serializer().descriptor.fingerprint(), Two.serializer().descriptor.fingerprint())
    }

    @Test
    fun `Renaming or reordering fields changes the fingerprint`() {
        assertNotEquals(described("Type", "a" to false, "b" to false).fingerprint(), described("Type", "b" to false, "a" to false).fingerprint())
        assertNotEquals(described("Type", "a" to false).fingerprint(), described("Type", "c" to false).fingerprint())
    }

    @Test
    fun `Reordering enum entries changes the fingerprint`() {
        assertEquals(enumeration("Colour", "Red", "Green").fingerprint(), enumeration("Colour", "Red", "Green").fingerprint())
        assertNotEquals(enumeration("Colour", "Red", "Green").fingerprint(), enumeration("Colour", "Green", "Red").fingerprint())
        assertNotEquals(enumeration("Colour", "Red", "Green").fingerprint(), enumeration("Colour", "Red", "Green", "Blue").fingerprint())
    }

    @Test
    fun `Nullability changes the fingerprint`() {
        assertNotEquals(described("Type", "a" to false).fingerprint(), described("Type", "a" to true).fingerprint())
    }

    @Test
    fun `Recursive types have a fingerprint`() {
        Nested.serializer().descriptor.fingerprint()
    }
}
