package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.MidiDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class MidiDefinitions : DefinitionsDecoder<MidiDefinition> {

    override lateinit var definitions: Array<MidiDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(yaml: Yaml = get(), path: String = Settings["definitions.midis"]): MidiDefinitions {
        timedLoad("midi definition") {
            decode(yaml, path) { id, key, _ ->
                MidiDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    override fun empty() = MidiDefinition.EMPTY

}