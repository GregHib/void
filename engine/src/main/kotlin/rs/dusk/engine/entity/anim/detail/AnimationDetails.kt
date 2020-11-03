package rs.dusk.engine.entity.anim.detail

import rs.dusk.cache.definition.data.AnimationDefinition
import rs.dusk.cache.definition.decoder.AnimationDecoder
import rs.dusk.engine.entity.DetailsDecoder

class AnimationDetails(
    override val decoder: AnimationDecoder,
    override val details: Map<String, Map<String, Any>>,
    override val names: Map<Int, String>
) : DetailsDecoder<AnimationDefinition, AnimationDecoder>