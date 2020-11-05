package rs.dusk.engine.entity.definition

import rs.dusk.cache.config.data.ContainerDefinition
import rs.dusk.cache.config.decoder.ContainerDecoder

class ContainerDefinitions(
    override val decoder: ContainerDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<ContainerDefinition, ContainerDecoder>