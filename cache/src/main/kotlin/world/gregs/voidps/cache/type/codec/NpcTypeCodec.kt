package world.gregs.voidps.cache.type.codec

import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.type.CacheCodec
import world.gregs.voidps.cache.type.data.NpcType

object NpcTypeCodec : CacheCodec<NpcType>() {
    override val index: Int = Index.NPCS

    override fun create(size: Int, block: (Int) -> NpcType) = Array(size, block)

    override fun create(id: Int) = NpcType(id)

    override fun group(index: Int) = index ushr 7

    override fun file(index: Int) = index and 0x7f

}