package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder

internal class AnimationDefinitionsTest : DefinitionsDecoderTest<AnimationDefinition, AnimationDecoder, AnimationDefinitions>() {

    override var decoder: AnimationDecoder = AnimationDecoder()
    override lateinit var definitions: Array<AnimationDefinition>
    override val id: String = "expression_yes"
    override val intId: Int = 9741

    override fun expected(): AnimationDefinition = AnimationDefinition(intId, stringId = id)

    override fun empty(): AnimationDefinition = AnimationDefinition(-1)

    override fun definitions(): AnimationDefinitions = AnimationDefinitions(definitions)

    override fun load(definitions: AnimationDefinitions) {
        val uri = AnimationDefinitionsTest::class.java.getResource("test-animation.toml")!!
        definitions.load(listOf(uri.path))
    }
}
