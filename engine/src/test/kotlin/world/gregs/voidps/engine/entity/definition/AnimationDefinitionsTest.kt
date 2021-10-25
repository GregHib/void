package world.gregs.voidps.engine.entity.definition

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder

internal class AnimationDefinitionsTest : DefinitionsDecoderTest<AnimationDefinition, AnimationDecoder, AnimationDefinitions>() {

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id)
    }

    override fun definition(id: Int): AnimationDefinition {
        return AnimationDefinition(id, stringId = id.toString())
    }

    override fun definitions(decoder: AnimationDecoder, id: Map<String, Map<String, Any>>, names: Map<Int, String>): AnimationDefinitions {
        return AnimationDefinitions(decoder).apply {
            load(id)
            this.names = names
        }
    }

}