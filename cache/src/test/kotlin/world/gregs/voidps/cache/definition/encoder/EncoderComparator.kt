package world.gregs.voidps.cache.definition.encoder

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index.ITEMS
import world.gregs.voidps.cache.definition.data.ItemDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ItemDecoder

class EncoderComparator {
    @Test
    fun compare() {
        val definition = ItemDefinitionFull()
        val encoder = ItemEncoder()

        val writer = BufferWriter()
        with(encoder) {
            writer.encode(definition)
        }

        val data = writer.array()

        val cache = mockk<Cache>(relaxed = true)
        startKoin {
            modules(
                module {
                    @Suppress("USELESS_CAST")
                    single(createdAtStart = true) { cache as Cache }
                },
            )
        }
        val decoder = ItemDecoder()
        every { cache.data(ITEMS, archive = any(), file = any()) } answers {
            if (arg<Int>(1) == decoder.getArchive(0) && arg<Int>(2) == decoder.getFile(0)) {
                data
            } else {
                null
            }
        }
        val defs = decoder.load(cache).getOrNull(0)
        println("Expected $definition")
        println("Actual   $defs")
    }
}
