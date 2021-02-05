package world.gregs.voidps.cache.format.definition

import kotlinx.serialization.*
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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
    data class CustomData(
        @Operation(1)
        val value: UByte = 0u,
        @Operation(2)
        val medium: Medium = Medium(0)
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
    fun `Decode custom types`() {
        val data = byteArrayOf(1, -100, 2, 15, 255.toByte(), 255.toByte())
        val def: CustomData = Definition.decodeFromByteArray(data)
        assertEquals(CustomData(156u, Medium(0xfffff)), def)
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

//    @Test
//    fun `Decode int array`() {
//        val data = byteArrayOf(1, 2, 3, 4)
//        val def: IntArrayData = Definition.decodeFromByteArray(data)
//        assertEquals(IntArrayData(intArrayOf(3, 4)), def)
//    }

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