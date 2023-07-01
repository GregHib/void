package world.gregs.voidps.engine.data.definition

import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.definition.Extra

abstract class DefinitionsDecoderTest<T, S : DefinitionDecoder<T>, D : DefinitionsDecoder<T>> where T : Definition, T : Extra {

    abstract val id: String

    abstract val intId: Int

    abstract fun expected(): T

    abstract fun empty(): T

    abstract fun definitions(): D

    abstract fun load(definitions: D)

    abstract var decoder: S

    abstract var definitions: Array<T>

    @BeforeEach
    open fun setup() {
        every { decoder.indices } returns (0..intId)
        every { decoder.get(any()) } returns empty()
        every { decoder.get(intId) } returns expected()
        every { decoder.getOrNull(any()) } returns null
        every { decoder.getOrNull(any()) } returns empty()
        every { decoder.getOrNull(intId) } returns expected()
    }

    @Test
    fun `Get definitions for string integer id`() {
        val definitions = definitions()
        load(definitions)
        val result = definitions.get(intId.toString())
        assertEquals(expected(), result)
    }

    @Test
    fun `Get definitions for name`() {
        val definitions = definitions()
        load(definitions)
        val result = definitions.get(intId.toString())
        assertEquals(expected(), result)
    }

    @Test
    fun `Get definitions without entry`() {
        val definitions = definitions()
        load(definitions)
        val result = definitions.get("-1")
        assertEquals(empty(), result)
    }

    @Test
    fun `Get null definitions`() {
        val definitions = definitions()
        load(definitions)
        val result = definitions.getOrNull(-1)
        assertNull(result)
    }

    @Test
    fun `Get null definitions by name`() {
        val definitions = definitions()
        load(definitions)
        val result = definitions.getOrNull("unknown")
        assertNull(result)
        assertFalse(definitions.contains("unknown"))
    }
}