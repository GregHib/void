package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder

class AnimationDefinitions(
    override val decoder: AnimationDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<AnimationDefinition, AnimationDecoder>