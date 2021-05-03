package world.gregs.voidps.engine.entity.definition

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder

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
        return GraphicDefinitions(decoder).apply {
            load(id)
            this.names = names
        }
    }

}