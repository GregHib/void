package rs.dusk.engine.entity.definition

import rs.dusk.cache.definition.data.AnimationDefinition
import rs.dusk.cache.definition.decoder.AnimationDecoder

class AnimationDefinitions(
    override val decoder: AnimationDecoder,
    override val extras: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DefinitionsDecoder<AnimationDefinition, AnimationDecoder>