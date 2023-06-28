package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.engine.data.decode
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.config.JingleDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.YamlParser

class JingleDefinitions : DefinitionsDecoder<JingleDefinition> {

    override lateinit var definitions: Array<JingleDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(parser: YamlParser = get(), path: String = getProperty("jingleDefinitionsPath")): JingleDefinitions {
        timedLoad("jingle definition") {
            decode(parser, path) { id, key, _ ->
                JingleDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    override fun empty() = JingleDefinition.EMPTY

}