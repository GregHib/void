package world.gregs.voidps.cache.format.definition

import kotlinx.serialization.*
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

    @Test
    fun `Decode unordered`() {
        val data = byteArrayOf(1, 0, 0, 0, 42, 2, 72, 105, 0)
        val def = Definition.decodeFromByteArray<TestDefinition>(data)
        assertEquals(TestDefinition("Hi", 42), def)
    }

    @Test
    fun `Decode duplicate operations`() {
        val data = byteArrayOf(1, 0, 0, 0, 42, 0, 0, 0, 32)
        val def = Definition.decodeFromByteArray<DuplicateData>(data)
        assertEquals(DuplicateData(42, 32), def)
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