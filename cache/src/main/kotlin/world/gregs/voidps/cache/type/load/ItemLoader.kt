package world.gregs.voidps.cache.type.load

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.type.TypeLoader
import world.gregs.voidps.cache.type.decode.ItemTypeDecoder
import world.gregs.voidps.cache.type.types.ItemType
import java.io.File

class ItemLoader(
    directory: File? = null
) : TypeLoader<ItemType>(directory, "items") {
    override val index = Index.ITEMS
    override val maxString: Int = 256
    override fun create(size: Int, block: (Int) -> ItemType?) = Array(size, block)
    override fun decoder(size: Int) = ItemTypeDecoder(size)
    override fun data(cache: Cache, index: Int) = cache.data(Index.ITEMS, index ushr 8, index and 0xff)
}