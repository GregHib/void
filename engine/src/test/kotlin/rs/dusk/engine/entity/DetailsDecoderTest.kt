package rs.dusk.engine.entity

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.cache.Definition
import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.definition.Details
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader

abstract class DetailsDecoderTest<T, D : DefinitionDecoder<T>, S : DetailsDecoder<T, D>> where T : Definition, T : Details {

    abstract fun map(id: Int): Map<String, Any>

    abstract fun detail(id: Int): T

    open fun populated(id: Int): Map<String, Any> {
        return mapOf(
            "id" to id
        )
    }

    private fun populatedDetails(id: Int): T {
        return detail(id).apply { this.details = populated(id) }
    }

    abstract fun details(decoder: D, id: Map<String, Map<String, Any>>, names: Map<Int, String>): S

    abstract fun loader(loader: FileLoader): TimedLoader<S>

    lateinit var decoder: D

    @BeforeEach
    open fun setup() {
        every { decoder.get(any()) } answers {
            val id: Int = arg(0)
            detail(id = id)
        }
    }

    @Test
    fun `Load details`() {
        val loader: FileLoader = mockk()
        every { loader.load<Map<String, Map<String, Any>>>("path") } returns mutableMapOf("name" to map(1))
        val detailLoader = loader(loader)
        val result = detailLoader.run("path")
        assertEquals(mapOf("name" to populated(1)), result.details)
        assertEquals(mapOf(1 to "name"), result.names)
    }

    @Test
    fun `Get details for id`() {
        val details = details(decoder, mapOf("name" to populated(1)), mapOf(1 to "name"))
        val result = details.get("name")
        assertEquals(populatedDetails(1), result)
    }

    @Test
    fun `Get details for name`() {
        val details = details(decoder, mapOf("name" to populated(1)), mapOf(1 to "name"))
        val result = details.get("name")
        assertEquals(populatedDetails(1), result)
    }

    @Test
    fun `Get details without entry`() {
        val details = details(decoder, mapOf(), mapOf())
        val result = details.get("name")
        assertEquals(detail(-1), result)
    }

    @Test
    fun `Get null details`() {
        val details = details(decoder, mapOf(), mapOf())
        val result = details.getOrNull("name")
        assertNull(result)
    }

    @Test
    fun `Get null details by name`() {
        val details = details(decoder, mapOf(), mapOf())
        val result = details.getOrNull("unknown")
        assertNull(result)
    }

    @Test
    fun `Get string id for int id`() {
        val details = details(decoder, mapOf(), mapOf(1 to "name"))
        val result = details.getName(1)
        assertEquals("name", result)
    }

    @Test
    fun `Get string id for int id without entry`() {
        val details = details(decoder, mapOf(), mapOf())
        val result = details.getName(1)
        assertEquals("", result)
    }

    @Test
    fun `Get null for int id without entry`() {
        val details = details(decoder, mapOf(), mapOf())
        val result = details.getNameOrNull(1)
        assertNull(result)
    }

    @Test
    fun `Get int id for string id`() {
        val details = details(decoder, mapOf("name" to mapOf("id" to 1)), mapOf())
        val result = details.getId("name")
        assertEquals(1, result)
    }

    @Test
    fun `Get int id for string id without entry`() {
        val details = details(decoder, mapOf(), mapOf())
        val result = details.getId("name")
        assertEquals(-1, result)
    }

    @Test
    fun `Get null for string id without entry`() {
        val details = details(decoder, mapOf(), mapOf())
        val result = details.getIdOrNull("name")
        assertNull(result)
    }
}