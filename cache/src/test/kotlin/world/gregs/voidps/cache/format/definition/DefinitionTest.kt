package world.gregs.voidps.cache.format.definition

import kotlinx.serialization.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.DataType
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier

@ExperimentalSerializationApi
internal class DefinitionTest {

    @Serializable
    data class TestDefinition(
        @Operation(2)
        val name: String = "null",
        @Operation(1)
        val model: Int = 0
    )

    @Serializable
    data class DuplicateData(
        @Operation(1)
        val model: Int = 0,
        @Operation(1)
        val type: Int = 0
    )

    @Serializable
    data class SettersData(
        @Setter(1)
        @Operation(1)
        val model: Int = 0,
        @Setter(0)
        @Operation(1)
        val valid: Boolean = true
    )

    @Serializable
    data class NullableData(
        @Operation(1)
        val name: String? = null,
    )

    @Serializable
    data class UnsignedData(
        @MetaData(DataType.BYTE, false, Modifier.NONE, Endian.BIG)
        @Operation(1)
        val value: Byte = 0,
    )

    @Serializable
    data class IntArrayData(
        @Operation(1)
        val data: IntArray? = null,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as IntArrayData

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }

    @Test
    fun `Decode unordered`() {
        val data = byteArrayOf(1, 0, 0, 0, 42, 2, 72, 105, 0)
        val def: TestDefinition = Definition.decodeFromByteArray(data)
        assertEquals(TestDefinition("Hi", 42), def)
    }

    @Test
    fun `Decode nullable`() {
        val data = byteArrayOf(1, 72, 105, 0)
        val def: NullableData = Definition.decodeFromByteArray(data)
        assertEquals(NullableData("Hi"), def)
    }

    @Test
    fun `Decode unsigned`() {
        val data = byteArrayOf(1, -100)
        val def: UnsignedData = Definition.decodeFromByteArray(data)
        assertEquals(UnsignedData(156.toByte()), def)
    }

    @Test
    fun `Decode with setters`() {
        val data = byteArrayOf(1)
        val def: SettersData = Definition.decodeFromByteArray(data)
        assertEquals(SettersData(1, false), def)
    }

    @Test
    fun `Decode duplicate operations`() {
        val data = byteArrayOf(1, 0, 0, 0, 42, 0, 0, 0, 32)
        val def: DuplicateData = Definition.decodeFromByteArray(data)
        assertEquals(DuplicateData(42, 32), def)
    }

    @Test
    fun `Decode int array`() {
        val data = byteArrayOf(1, 2, 3, 4)
        val def: IntArrayData = Definition.decodeFromByteArray(data)
        assertEquals(IntArrayData(intArrayOf(3, 4)), def)
    }

    @Test
    fun `Encode ignores default values`() {
        val def = TestDefinition(model = 1234)
        val data = Definition.encodeToByteArray(def)
        assertArrayEquals(byteArrayOf(1, 0, 0, 4, -46), data)
    }

    @Test
    fun `Multiple types`() {
        val data = TestDefinition("Hi", 42)
        assertMappedAndRestored(
            byteArrayOf(2, 72, 105, 0, 1, 0, 0, 0, 42),
            data,
            TestDefinition.serializer()
        )
    }

    private inline fun <reified T : Any> assertMappedAndRestored(
        expectedArray: ByteArray,
        obj: T,
        serializer: KSerializer<T>
    ) {
        val array = Definition.encodeToByteArray(serializer, obj)
        assertArrayEquals(expectedArray, array)
        val unmap = Definition.decodeFromByteArray(serializer, array)
        assertEquals(obj, unmap)
    }
}