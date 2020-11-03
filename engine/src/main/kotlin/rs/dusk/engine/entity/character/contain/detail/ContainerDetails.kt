package rs.dusk.engine.entity.character.contain.detail

import rs.dusk.cache.config.data.ItemContainerDefinition
import rs.dusk.cache.config.decoder.ItemContainerDecoder
import rs.dusk.engine.entity.DetailsDecoder

class ContainerDetails(
    override val decoder: ItemContainerDecoder,
    override val details: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DetailsDecoder<ItemContainerDefinition, ItemContainerDecoder>