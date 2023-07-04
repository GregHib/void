package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.yaml.Yaml

internal class AnimationDefinitionsTest : DefinitionsDecoderTest<AnimationDefinition, AnimationDecoder, AnimationDefinitions>() {

    override var decoder: AnimationDecoder = AnimationDecoder()
    override lateinit var definitions: Array<AnimationDefinition>
    override val id: String = "expression_yes"
    override val intId: Int = 9741

    override fun expected(): AnimationDefinition {
        return AnimationDefinition(intId, stringId = id, extras = mapOf("id" to intId))
    }

    override fun empty(): AnimationDefinition {
        return AnimationDefinition(-1)
    }

    override fun definitions(): AnimationDefinitions {
        return AnimationDefinitions(definitions)
    }

    override fun load(definitions: AnimationDefinitions) {
        definitions.load(Yaml(), "../data/definitions/animations.yml")
    }

}
