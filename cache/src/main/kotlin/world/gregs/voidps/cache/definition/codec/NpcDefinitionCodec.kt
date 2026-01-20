package world.gregs.voidps.cache.definition.codec

import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.NPCDefinitionFull
import world.gregs.voidps.cache.type.CacheCodec

object NpcDefinitionCodec : CacheCodec<NPCDefinitionFull>() {
    override val index: Int = Index.NPCS

    override fun create(size: Int, block: (Int) -> NPCDefinitionFull) = Array(size, block)

    override fun create(id: Int) = NPCDefinitionFull(id)

    override fun group(index: Int) = index ushr 7

    override fun file(index: Int) = index and 0x7f

}