package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getProperty

class MidiDefinitions : Extras {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>

    fun load(loader: FileLoader = get(), path: String = getProperty("midiDefinitionsPath")): MidiDefinitions {
        timedLoad("midi definition") {
            load(loader.load<Map<String, Any>>(path).mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        names = data.map { it.value["id"] as Int to it.key }.toMap()
        this.extras = data
        return names.size
    }

}