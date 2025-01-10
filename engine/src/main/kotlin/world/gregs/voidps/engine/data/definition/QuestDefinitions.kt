package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.config.data.QuestDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class QuestDefinitions : DefinitionsDecoder<QuestDefinition> {

    override lateinit var definitions: Array<QuestDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(yaml: Yaml = get(), path: String = Settings["definitions.quests"]): QuestDefinitions {
        timedLoad("quest definition") {
            decode(yaml, path) { id, key, extras ->
                QuestDefinition(id = id, stringId = key, extras = extras)
            }
        }
        return this
    }

    override fun empty() = QuestDefinition.EMPTY

}