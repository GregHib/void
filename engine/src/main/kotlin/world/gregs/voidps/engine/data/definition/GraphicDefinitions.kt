package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class GraphicDefinitions(
    override var definitions: Array<GraphicDefinition>
) : DefinitionsDecoder<GraphicDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = GraphicDefinition.EMPTY

    fun load(yaml: Yaml = get(), path: String = Settings["graphicDefinitionsPath"]): GraphicDefinitions {
        timedLoad("graphic extra") {
            decode(yaml, path)
        }
        return this
    }

}