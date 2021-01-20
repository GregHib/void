package world.gregs.void.engine.entity.definition

import world.gregs.void.cache.definition.data.ItemDefinition
import world.gregs.void.cache.definition.decoder.ItemDecoder

class ItemDefinitions(
    override val decoder: ItemDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<ItemDefinition, ItemDecoder>