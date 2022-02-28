package world.gregs.voidps.engine.entity.definition

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.engine.data.FileStorage

internal class GraphicDefinitionsTest : DefinitionsDecoderTest<GraphicDefinition, GraphicDecoder, GraphicDefinitions>() {

    override lateinit var decoder: GraphicDecoder
    override val id: String = "teleport_modern"
    override val intId: Int = 1576

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun expected(): GraphicDefinition {
        return GraphicDefinition(intId, stringId = id, extras = mapOf("id" to intId))
    }

    override fun empty(): GraphicDefinition {
        return GraphicDefinition(-1)
    }

    override fun definitions(): GraphicDefinitions {
        return GraphicDefinitions(decoder)
    }

    override fun load(definitions: GraphicDefinitions) {
        definitions.load(FileStorage(), "../data/definitions/graphics.yml")
    }

}
