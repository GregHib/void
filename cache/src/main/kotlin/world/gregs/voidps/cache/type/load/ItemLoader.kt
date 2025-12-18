package world.gregs.voidps.cache.type.load

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.type.LoaderConfig
import world.gregs.voidps.cache.type.decode.ItemTypeDecoder
import world.gregs.voidps.cache.type.types.ItemType

class ItemLoader(
    override val paths: List<String>,
    override val lastModified: Long,
) : LoaderConfig<ItemType> {
    override val name = "items"
    override val bufferSize = 2_000_000
    override val index = Index.ITEMS
    override fun size(cache: Cache) = cache.lastArchiveId(index) * 256 + (cache.fileCount(index, cache.lastArchiveId(index)))
    override fun file(id: Int) = id and 0xff
    override fun archive(id: Int) = id ushr 8
    override fun create(size: Int, block: (Int) -> ItemType?) = Array(size, block)
    override fun decoder() = ItemTypeDecoder()
}