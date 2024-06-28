package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.config.data.QuestDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class QuestDefinitions : DefinitionsDecoder<QuestDefinition> {

    override lateinit var definitions: Array<QuestDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(yaml: Yaml = get(), path: String = getProperty("questDefinitionsPath")): QuestDefinitions {
        timedLoad("quest definition") {
            decode(yaml, path) { id, key, _ ->
                QuestDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    override fun empty() = QuestDefinition.EMPTY
}