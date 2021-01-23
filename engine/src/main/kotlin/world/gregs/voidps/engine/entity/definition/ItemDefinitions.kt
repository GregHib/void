package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder

class ItemDefinitions(
    override val decoder: ItemDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<ItemDefinition, ItemDecoder>