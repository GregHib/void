package world.gregs.void.engine.entity.definition

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.void.cache.definition.data.AnimationDefinition
import world.gregs.void.cache.definition.decoder.AnimationDecoder
import world.gregs.void.engine.TimedLoader
import world.gregs.void.engine.data.file.FileLoader
import world.gregs.void.engine.entity.definition.load.AnimationDefinitionLoader

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