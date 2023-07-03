package world.gregs.voidps.cache.active.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Indices

class HuffmanEncoder : IndexEncoder(Indices.HUFFMAN) {
    override fun encode(writer: Writer, cache: Cache) {
        val data = cache.getFile(index, 1) ?: return
        encode(writer, index, 1, 0, data)
    }
}