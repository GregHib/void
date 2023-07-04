package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.engine.data.definition.DefinitionsDecoderTest
import world.gregs.yaml.Yaml

internal class GraphicDefinitionsTest : DefinitionsDecoderTest<GraphicDefinition, GraphicDecoder, GraphicDefinitions>() {

    override var decoder: GraphicDecoder = GraphicDecoder()
    override lateinit var definitions: Array<GraphicDefinition>
    override val id: String = "teleport_modern"
    override val intId: Int = 1576

    override fun expected(): GraphicDefinition {
        return GraphicDefinition(intId, stringId = id, extras = mapOf("id" to intId))
    }

    override fun empty(): GraphicDefinition {
        return GraphicDefinition(-1)
    }

    override fun definitions(): GraphicDefinitions {
        return GraphicDefinitions(definitions)
    }

    override fun load(definitions: GraphicDefinitions) {
        definitions.load(Yaml(), "../data/definitions/graphics.yml")
    }

}
