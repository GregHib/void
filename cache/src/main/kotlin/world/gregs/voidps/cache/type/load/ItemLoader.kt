package world.gregs.voidps.cache.type.load

import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.type.TypeLoader
import world.gregs.voidps.cache.type.decode.ItemTypeDecoder
import world.gregs.voidps.cache.type.types.ItemType
import java.io.File

class ItemLoader(
    directory: File
) : TypeLoader<ItemType>(directory, "items") {
    override val bufferSize = 3_000_000
    override val index = Index.ITEMS
    override val maxString: Int = 256
    override fun file(id: Int) = id and 0xff
    override fun archive(id: Int) = id ushr 8
    override fun create(size: Int, block: (Int) -> ItemType?) = Array(size, block)
    override fun decoder() = ItemTypeDecoder()
}