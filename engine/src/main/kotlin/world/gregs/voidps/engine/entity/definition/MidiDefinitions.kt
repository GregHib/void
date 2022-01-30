package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.entity.definition.config.MidiDefinition
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class MidiDefinitions : Definitions<MidiDefinition> {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>

    fun load(storage: FileStorage = get(), path: String = getProperty("midiDefinitionsPath")): MidiDefinitions {
        timedLoad("midi definition") {
            load(storage.load<Map<String, Any>>(path).mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        names = data.map { it.value["id"] as Int to it.key }.toMap()
        this.extras = data
        return names.size
    }

    override fun decodeOrNull(name: String, id: Int) = if (extras.containsKey(name)) MidiDefinition(id, stringId = names.getValue(id)) else null

    override fun decode(name: String, id: Int) = decodeOrNull(name, id) ?: MidiDefinition(id, stringId = name)

}