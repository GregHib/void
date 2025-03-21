package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder

internal class GraphicDefinitionsTest : DefinitionsDecoderTest<GraphicDefinition, GraphicDecoder, GraphicDefinitions>() {

    override var decoder: GraphicDecoder = GraphicDecoder()
    override lateinit var definitions: Array<GraphicDefinition>
    override val id: String = "teleport_modern"
    override val intId: Int = 1576

    override fun expected(): GraphicDefinition {
        return GraphicDefinition(intId, stringId = id)
    }

    override fun empty(): GraphicDefinition {
        return GraphicDefinition(-1)
    }

    override fun definitions(): GraphicDefinitions {
        return GraphicDefinitions(definitions)
    }

    override fun load(definitions: GraphicDefinitions) {
        val uri = GraphicDefinitionsTest::class.java.getResource("test-gfx.toml")!!
        definitions.load(uri.path)
    }

}
