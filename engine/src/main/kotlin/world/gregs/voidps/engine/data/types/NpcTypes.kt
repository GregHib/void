package world.gregs.voidps.engine.data.types

import world.gregs.voidps.cache.definition.data.NPCDefinitionFull
import world.gregs.voidps.cache.definition.decoder.NpcDefinitionCodec
import world.gregs.voidps.cache.type.NpcType
import world.gregs.voidps.cache.type.codec.NpcTypeCodec
import world.gregs.voidps.engine.data.types.keys.NpcParams

object NpcTypes : ParamTypes<NpcType, NPCDefinitionFull>() {
    override val extension = "definitions.npcs"
    override val params = NpcParams
    override val definitionCodec = NpcDefinitionCodec
    override val typeCodec = NpcTypeCodec
    override val maxStringSize = 250
    override val size: Int = 5_000_000
    override fun create(size: Int, array: Array<NPCDefinitionFull>) = Array(size) { NpcType(array[it]) }
    override fun create(size: Int) = Array(size) { NpcType(it) }
}