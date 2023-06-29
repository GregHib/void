package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.engine.data.decode
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.config.MidiDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class MidiDefinitions : DefinitionsDecoder<MidiDefinition> {

    override lateinit var definitions: Array<MidiDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(yaml: Yaml = get(), path: String = getProperty("midiDefinitionsPath")): MidiDefinitions {
        timedLoad("midi definition") {
            decode(yaml, path) { id, key, _ ->
                MidiDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    override fun empty() = MidiDefinition.EMPTY

}