package rs.dusk.engine.entity.anim.detail

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import rs.dusk.cache.definition.data.AnimationDefinition
import rs.dusk.cache.definition.decoder.AnimationDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.DefinitionsDecoderTest

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
        return AnimationDefinition(id)
    }

    override fun definitions(decoder: AnimationDecoder, id: Map<String, Map<String, Any>>, names: Map<Int, String>): AnimationDefinitions {
        return AnimationDefinitions(decoder, id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<AnimationDefinitions> {
        return AnimationDefinitionLoader(loader, decoder)
    }

}