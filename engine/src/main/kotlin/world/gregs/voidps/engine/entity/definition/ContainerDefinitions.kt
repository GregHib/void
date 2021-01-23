package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder

class ContainerDefinitions(
    override val decoder: ContainerDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<ContainerDefinition, ContainerDecoder>