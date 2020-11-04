package rs.dusk.engine.entity.gfx.detail

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import rs.dusk.cache.definition.data.GraphicDefinition
import rs.dusk.cache.definition.decoder.GraphicDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.DefinitionsDecoderTest

internal class GraphicDefinitionsTest : DefinitionsDecoderTest<GraphicDefinition, GraphicDecoder, GraphicDefinitions>() {

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id)
    }

    override fun definition(id: Int): GraphicDefinition {
        return GraphicDefinition(id)
    }

    override fun definitions(decoder: GraphicDecoder, id: Map<String, Map<String, Any>>, names: Map<Int, String>): GraphicDefinitions {
        return GraphicDefinitions(decoder, id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<GraphicDefinitions> {
        return GraphicDefinitionLoader(loader, decoder)
    }

}