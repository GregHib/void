package rs.dusk.engine.entity.character.contain.detail

import rs.dusk.cache.config.data.ItemContainerDefinition
import rs.dusk.cache.config.decoder.ItemContainerDecoder
import rs.dusk.engine.entity.DefinitionsDecoder

class ContainerDefinitions(
    override val decoder: ItemContainerDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<ItemContainerDefinition, ItemContainerDecoder>