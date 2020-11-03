package rs.dusk.engine.entity.gfx.detail

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import rs.dusk.cache.definition.data.GraphicDefinition
import rs.dusk.cache.definition.decoder.GraphicDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.DetailsDecoderTest

internal class GraphicDetailsTest : DetailsDecoderTest<GraphicDefinition, GraphicDecoder, GraphicDetails>() {

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id)
    }

    override fun detail(id: Int): GraphicDefinition {
        return GraphicDefinition(id)
    }

    override fun details(decoder: GraphicDecoder, id: Map<String, Map<String, Any>>, names: Map<Int, String>): GraphicDetails {
        return GraphicDetails(decoder, id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<GraphicDetails> {
        return GraphicDetailsLoader(loader, decoder)
    }

}