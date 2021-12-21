package world.gregs.voidps.engine.entity.definition

import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.definition.Extra

abstract class DefinitionsDecoderTest<T, D : DefinitionDecoder<T>, S : DefinitionsDecoder<T, D>> where T : Definition, T : Extra {

    open val allowsModification: Boolean = false

    abstract fun map(id: Int): Map<String, Any>

    abstract fun definition(id: Int): T

    open fun populated(id: Int): Map<String, Any> {
        return mapOf(
            "id" to id
        )
    }

    private fun populatedDefinition(id: Int): T {
        return definition(id).apply {
            this.extras = populated(id)
            this.stringId = "name"
        }
    }

    abstract fun definitions(decoder: D): S

    abstract fun load(definitions: S, id: Map<String, Map<String, Any>>, names: Map<Int, String>)


    lateinit var decoder: D

    @BeforeEach
    open fun setup() {
        every { decoder.get(any()) } answers {
            val id: Int = arg(0)
            definition(id = id)
        }
        every { decoder.getOrNull(any()) } answers {
            val id: Int = arg(0)
            definition(id = id)
        }
    }

    @Test
    fun `Get definitions for string integer id`() {
        val definitions = definitions(decoder)
        load(definitions, mapOf("name" to map(1)), mapOf(1 to "name"))
        val result = definitions.get("1")
        assertEquals(populatedDefinition(1), result)
    }

    @Test
    fun `Get definitions for name`() {
        val definitions = definitions(decoder)
        load(definitions, mapOf("name" to map(1)), mapOf(1 to "name"))
        val result = definitions.get("1")
        assertEquals(populatedDefinition(1), result)
    }

    @Test
    fun `Get definitions without entry`() {
        val definitions = definitions(decoder)
        load(definitions, mapOf(), mapOf())
        val result = definitions.get("-1")
        assertEquals(definition(-1), result)
        assertFalse(definitions.contains("name"))
    }

    @Test
    fun `Get null definitions`() {
        val definitions = definitions(decoder)
        load(definitions, mapOf(), mapOf())
        val result = definitions.getOrNull("name")
        assertNull(result)
        assertFalse(definitions.contains("name"))
    }

    @Test
    fun `Contains definitions`() {
        val definitions = definitions(decoder)
        load(definitions, mapOf("name" to map(1)), mapOf())
        assertTrue(definitions.contains("name"))
    }

    @Test
    fun `Get null definitions by name`() {
        val definitions = definitions(decoder)
        load(definitions, mapOf(), mapOf())
        val result = definitions.getOrNull("unknown")
        assertNull(result)
    }

    @Test
    fun `Append to data`() {
        if (!allowsModification) {
            return
        }
        val definitions = definitions(decoder)
        definitions.modifications.add { it["test"] = true }
        load(definitions, mapOf("name" to map(1)), mapOf(1 to "name"))
        val result = definitions.get("1")
        assertTrue(result.has("test"))
        assertEquals(true, result["test"])
    }

    @Test
    fun `Modify existing data`() {
        if (!allowsModification) {
            return
        }
        val definitions = definitions(decoder)
        definitions.modifications["mutable"] = { true }
        load(definitions, mapOf("name" to map(1)), mapOf(1 to "name"))
        val result = definitions.get("1")
        assertTrue(result.has("mutable"))
        assertEquals(true, result["mutable"])
    }
}