package world.gregs.void.engine.entity.definition

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.void.cache.Definition
import world.gregs.void.cache.DefinitionDecoder
import world.gregs.void.cache.definition.Extra
import world.gregs.void.engine.TimedLoader
import world.gregs.void.engine.data.file.FileLoader

abstract class DefinitionsDecoderTest<T, D : DefinitionDecoder<T>, S : DefinitionsDecoder<T, D>> where T : Definition, T : Extra {

    abstract fun map(id: Int): Map<String, Any>

    abstract fun definition(id: Int): T

    open fun populated(id: Int): Map<String, Any> {
        return mapOf(
            "id" to id
        )
    }

    private fun populatedDefinition(id: Int): T {
        return definition(id).apply { this.extras = populated(id) }
    }

    abstract fun definitions(decoder: D, id: Map<String, Map<String, Any>>, names: Map<Int, String>): S

    abstract fun loader(loader: FileLoader): TimedLoader<S>

    lateinit var decoder: D

    @BeforeEach
    open fun setup() {
        every { decoder.get(any()) } answers {
            val id: Int = arg(0)
            definition(id = id)
        }
    }

    @Test
    fun `Load details`() {
        val loader: FileLoader = mockk()
        every { loader.load<Map<String, Map<String, Any>>>("path") } returns mutableMapOf("name" to map(1))
        val detailLoader = loader(loader)
        val result = detailLoader.run("path")
        assertEquals(mapOf("name" to populated(1)), result.extras)
        assertEquals(mapOf(1 to "name"), result.names)
    }

    @Test
    fun `Get details for id`() {
        val details = definitions(decoder, mapOf("name" to populated(1)), mapOf(1 to "name"))
        val result = details.get("name")
        assertEquals(populatedDefinition(1), result)
    }

    @Test
    fun `Get details for name`() {
        val details = definitions(decoder, mapOf("name" to populated(1)), mapOf(1 to "name"))
        val result = details.get("name")
        assertEquals(populatedDefinition(1), result)
    }

    @Test
    fun `Get details without entry`() {
        val details = definitions(decoder, mapOf(), mapOf())
        val result = details.get("name")
        assertEquals(definition(-1), result)
    }

    @Test
    fun `Get null details`() {
        val details = definitions(decoder, mapOf(), mapOf())
        val result = details.getOrNull("name")
        assertNull(result)
    }

    @Test
    fun `Get null details by name`() {
        val details = definitions(decoder, mapOf(), mapOf())
        val result = details.getOrNull("unknown")
        assertNull(result)
    }

    @Test
    fun `Get string id for int id`() {
        val details = definitions(decoder, mapOf(), mapOf(1 to "name"))
        val result = details.getName(1)
        assertEquals("name", result)
    }

    @Test
    fun `Get string id for int id without entry`() {
        val details = definitions(decoder, mapOf(), mapOf())
        val result = details.getName(1)
        assertEquals("", result)
    }

    @Test
    fun `Get null for int id without entry`() {
        val details = definitions(decoder, mapOf(), mapOf())
        val result = details.getNameOrNull(1)
        assertNull(result)
    }

    @Test
    fun `Get int id for string id`() {
        val details = definitions(decoder, mapOf("name" to mapOf("id" to 1)), mapOf())
        val result = details.getId("name")
        assertEquals(1, result)
    }

    @Test
    fun `Get int id for string id without entry`() {
        val details = definitions(decoder, mapOf(), mapOf())
        val result = details.getId("name")
        assertEquals(-1, result)
    }

    @Test
    fun `Get null for string id without entry`() {
        val details = definitions(decoder, mapOf(), mapOf())
        val result = details.getIdOrNull("name")
        assertNull(result)
    }
}