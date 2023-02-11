package world.gregs.voidps.engine.data.definition.extra

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.DefinitionsDecoderTest

internal class AnimationDefinitionsTest : DefinitionsDecoderTest<AnimationDefinition, AnimationDecoder, AnimationDefinitions>() {

    override lateinit var decoder: AnimationDecoder
    override val id: String = "expression_yes"
    override val intId: Int = 9741

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun expected(): AnimationDefinition {
        return AnimationDefinition(intId, stringId = id, extras = mapOf("id" to intId))
    }

    override fun empty(): AnimationDefinition {
        return AnimationDefinition(-1)
    }

    override fun definitions(): AnimationDefinitions {
        return AnimationDefinitions(decoder)
    }

    override fun load(definitions: AnimationDefinitions) {
        definitions.load(FileStorage(), "../data/definitions/animations.yml")
    }

}
