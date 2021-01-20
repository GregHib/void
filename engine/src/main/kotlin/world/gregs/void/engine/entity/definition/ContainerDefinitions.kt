package world.gregs.void.engine.entity.definition

import world.gregs.void.cache.config.data.ContainerDefinition
import world.gregs.void.cache.config.decoder.ContainerDecoder

class ContainerDefinitions(
    override val decoder: ContainerDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<ContainerDefinition, ContainerDecoder>