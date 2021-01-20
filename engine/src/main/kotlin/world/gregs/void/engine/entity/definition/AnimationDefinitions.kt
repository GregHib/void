package world.gregs.void.engine.entity.definition

import world.gregs.void.cache.definition.data.AnimationDefinition
import world.gregs.void.cache.definition.decoder.AnimationDecoder

class AnimationDefinitions(
    override val decoder: AnimationDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<AnimationDefinition, AnimationDecoder>