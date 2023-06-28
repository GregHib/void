package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class GraphicDefinitions(
    decoder: GraphicDecoder
) : DefinitionsDecoder<GraphicDefinition> {

    override lateinit var definitions: Array<GraphicDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("graphic definition", definitions.size, start)
    }

    override fun empty() = GraphicDefinition.EMPTY

    fun load(yaml: Yaml = get(), path: String = getProperty("graphicDefinitionsPath")): GraphicDefinitions {
        timedLoad("graphic extra") {
            decode(yaml, path)
        }
        return this
    }

}