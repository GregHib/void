package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.JingleDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class JingleDefinitions : DefinitionsDecoder<JingleDefinition> {

    override lateinit var definitions: Array<JingleDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(yaml: Yaml = get(), path: String = Settings["jingleDefinitionsPath"]): JingleDefinitions {
        timedLoad("jingle definition") {
            decode(yaml, path) { id, key, _ ->
                JingleDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    override fun empty() = JingleDefinition.EMPTY

}