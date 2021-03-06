package world.gregs.voidps.engine.entity.definition

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.engine.TimedLoader
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.load.GraphicDefinitionLoader

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