package rs.dusk.engine.entity

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader

abstract class EntityDetailsTest<T : EntityDetail, S : EntityDetails> {

    abstract fun map(id: Int): Map<String, Any>

    abstract fun detail(id: Int): T

    abstract fun details(id: Map<Int, T>, names: BiMap<Int, String>): S

    abstract fun loader(loader: FileLoader): TimedLoader<S>

    @Test
    fun `Load details`() {
        val loader: FileLoader = mockk()
        every { loader.load<Map<String, Map<String, Any>>>("path") } returns mutableMapOf("name" to map(1))
        val detailLoader = loader(loader)
        val result = detailLoader.run("path")
        assertEquals(mapOf(1 to detail(1)), result.details)
        assertEquals(HashBiMap.create(mapOf(1 to "name")), result.names)
    }

    @Test
    fun `Get details for id`() {
        val details = details(mapOf(1 to detail(1)), HashBiMap.create(mapOf(1 to "name")))
        val result = details.get(1)
        assertEquals(detail(1), result)
    }

    @Test
    fun `Get details without entry`() {
        val details = details(mapOf(), HashBiMap.create(mapOf()))
        val result = details.get(2)
        assertEquals(detail(2), result)
    }

    @Test
    fun `Get null details`() {
        val details = details(mapOf(), HashBiMap.create(mapOf()))
        val result = details.getOrNull(2)
        assertNull(result)
    }

    @Test
    fun `Get string id for int id`() {
        val details = details(mapOf(), HashBiMap.create(mapOf(1 to "name")))
        val result = details.getName(1)
        assertEquals("name", result)
    }

    @Test
    fun `Get string id for int id without entry`() {
        val details = details(mapOf(), HashBiMap.create(mapOf()))
        val result = details.getName(1)
        assertEquals("", result)
    }

    @Test
    fun `Get null for int id without entry`() {
        val details = details(mapOf(), HashBiMap.create(mapOf()))
        val result = details.getNameOrNull(1)
        assertNull(result)
    }

    @Test
    fun `Get int id for string id`() {
        val details = details(mapOf(), HashBiMap.create(mapOf(1 to "name")))
        val result = details.getId("name")
        assertEquals(1, result)
    }

    @Test
    fun `Get int id for string id without entry`() {
        val details = details(mapOf(), HashBiMap.create(mapOf()))
        val result = details.getId("name")
        assertEquals(-1, result)
    }

    @Test
    fun `Get null for string id without entry`() {
        val details = details(mapOf(), HashBiMap.create(mapOf()))
        val result = details.getIdOrNull("name")
        assertNull(result)
    }
}