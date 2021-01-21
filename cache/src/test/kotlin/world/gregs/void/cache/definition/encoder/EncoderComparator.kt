package world.gregs.void.cache.definition.encoder

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.void.buffer.write.BufferWriter
import world.gregs.void.cache.Cache
import world.gregs.void.cache.Indices.ITEMS
import world.gregs.void.cache.definition.data.ItemDefinition
import world.gregs.void.cache.definition.decoder.ItemDecoder

/**
 * @author GregHib <greg@gregs.world>
 * @since April 23, 2020
 */
class EncoderComparator {
    @Test
    fun compare() {
        val definition = ItemDefinition()
        val encoder = ItemEncoder()

        val writer = BufferWriter()
        with(encoder) {
            writer.encode(definition)
        }

        val data = writer.buffer.array()

        val cache = mockk<Cache>(relaxed = true)
        startKoin {
            modules(module {
                @Suppress("USELESS_CAST")
                single(createdAtStart = true) { cache as Cache }
            })
        }
        val decoder = ItemDecoder(cache)
        every { cache.getFile(ITEMS, archive = any(), file = any()) } answers {
            if (arg<Int>(1) == decoder.getArchive(0) && arg<Int>(2) == decoder.getFile(0)) {
                data
            } else {
                null
            }
        }
        val defs = decoder.getOrNull(0)
        println("Expected $definition")
        println("Actual   $defs")
    }
}